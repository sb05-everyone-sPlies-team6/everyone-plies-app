package team6.finalproject.domain.user.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.TargetType;
import team6.finalproject.domain.notification.event.NotificationCreatedEvent;
import team6.finalproject.domain.notification.repository.NotificationRepository;
import team6.finalproject.domain.user.dto.CursorResponse;
import team6.finalproject.domain.user.dto.PasswordChangeRequest;
import team6.finalproject.domain.user.dto.UserCreateRequest;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.dto.UserLockUpdateRequest;
import team6.finalproject.domain.user.dto.UserProfileResponse;
import team6.finalproject.domain.user.dto.UserRoleUpdateRequest;
import team6.finalproject.domain.user.dto.UserUpdateRequest;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.domain.common.S3Folder;
import team6.finalproject.domain.common.S3Service;
import team6.finalproject.global.security.jwt.JwtRegistry;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;
  private final S3Service s3Service;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public UserDto create(UserCreateRequest request) {

    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists!");
    }

    String encodePassword = passwordEncoder.encode(request.password());
    User user = new User(request.email(),encodePassword, request.name());

    userRepository.save(user);
    return UserDto.from(user);
  }

  @Transactional(readOnly = true)
  public CursorResponse<UserDto> findAll(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {
    return userRepository.findAll(emailLike,  role, isLocked, cursor, idAfter, limit, sortDirection, sortBy);
  }

  @Transactional(readOnly = true)
  public UserProfileResponse findByIdForProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    UserDto userDto = UserDto.from(user);
    return UserProfileResponse.from(userDto); // 프론트용 String id
    }

  @Transactional
  public void changePassword(Long userId, PasswordChangeRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.changePassword(passwordEncoder.encode(request.password()));
  }

  @Transactional
  public UserProfileResponse updateProfile(Long userId, UserUpdateRequest request, MultipartFile file) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    String url = null;
    if (file != null && !file.isEmpty()) {
      url = s3Service.upload(file, S3Folder.PROFILE.toString());
    }

    user.updateProfile(request.name(), url);

    UserDto userDto = UserDto.from(user);
    return UserProfileResponse.from(userDto);
  }

  @Transactional
  public void updateRole(Long userId, UserRoleUpdateRequest request) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.getRole() == request.role()) {
      return;
    }

    user.changeRole(request.role());
    jwtRegistry.invalidateJwtInformationByUserId(userId);

    Notification notification = new Notification(
        user,
        "ROLE CHANGED",
        user.getName() + "님의 권한이 " + user.getRole() + "로 변환되었습니다.",
        Level.INFO,
        user.getId(),
        TargetType.ROLE_CHANGED
    );

    Notification saved = notificationRepository.save(notification);
    NotificationDto dto = NotificationDto.from(saved);
    eventPublisher.publishEvent(new NotificationCreatedEvent(dto));
  }

  @Transactional
  public void updateLocked(Long userId, UserLockUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.getLocked() == request.locked()) {
      return;
    }

    user.changeLock(request.locked());
    if (request.locked()) {
      jwtRegistry.invalidateJwtInformationByUserId(userId);
    }
  }

  @Transactional(readOnly = true)
  public User getUserByEmail(String email) {
      return userRepository.findByEmail(email)
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
  }
}

package team6.finalproject.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import team6.finalproject.domain.user.dto.CursorResponse;
import team6.finalproject.domain.user.dto.PasswordChangeRequest;
import team6.finalproject.domain.user.dto.UserCreateRequest;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.dto.UserLockUpdateRequest;
import team6.finalproject.domain.user.dto.UserRoleUpdateRequest;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.global.security.jwt.JwtRegistry;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final JwtRegistry jwtRegistry;

  @Transactional
  public UserDto create(UserCreateRequest request) {

    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists!");
    }

    User user = new User(request.email(), request.password(), request.name());

    userRepository.save(user);
    return UserDto.from(user);
  }

  @Transactional(readOnly = true)
  public CursorResponse<UserDto> findAll(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {
    return userRepository.findAll(emailLike,  role, isLocked, cursor, idAfter, limit, sortDirection, sortBy);
  }

  @Transactional(readOnly = true)
  public UserDto findById(Long userId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    return UserDto.from(user);
  }

  @Transactional
  public void changePassword(Long userId, PasswordChangeRequest request) {

    // JWT 본인확인 구현 필요

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.changePassword(request.password());
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

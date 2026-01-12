package team6.finalproject.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import team6.finalproject.domain.user.dto.*;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.global.security.jwt.JwtRegistry;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtRegistry jwtRegistry;

    @Transactional
    public User create(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists!");
        }

        User user = new User(request.email(), request.password(), request.name());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return user;
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
    public UserSummary getUserSummary(String userId) {
        // userId가 이메일(혹은 String)일 경우 findByEmail을 사용하여 사용자를 조회
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserSummary(
                user.getId().toString(),  // userId는 String으로 처리
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
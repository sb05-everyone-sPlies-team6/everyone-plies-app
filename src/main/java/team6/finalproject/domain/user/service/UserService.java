package team6.finalproject.domain.user.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import team6.finalproject.domain.user.dto.UserCreateRequest;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

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
}

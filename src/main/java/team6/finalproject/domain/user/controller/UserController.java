package team6.finalproject.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.domain.user.dto.PasswordChangeRequest;
import team6.finalproject.domain.user.dto.UserCreateRequest;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<User> create(@RequestBody @Valid UserCreateRequest request) {
    User user = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<User> findById(@PathVariable Long userId) {
    User user = userService.findById(userId);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PatchMapping("{userId}/password")
  public ResponseEntity<Void> changePassword(@PathVariable Long userId, @RequestBody @Valid PasswordChangeRequest request) {
    userService.changePassword(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}

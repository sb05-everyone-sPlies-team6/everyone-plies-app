package team6.finalproject.domain.user.controller;

import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team6.finalproject.domain.user.dto.CursorResponse;
import team6.finalproject.domain.user.dto.PasswordChangeRequest;
import team6.finalproject.domain.user.dto.UserCreateRequest;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.dto.UserLockUpdateRequest;
import team6.finalproject.domain.user.dto.UserRoleUpdateRequest;
import team6.finalproject.domain.user.dto.UserUpdateRequest;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.service.UserService;
import team6.finalproject.global.security.MoplUserDetails;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> create(@RequestBody @Valid UserCreateRequest request) {
    UserDto user = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<CursorResponse<UserDto>> findAll(String emailLike, Role role, Boolean isLocked,
      String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {
    CursorResponse<UserDto> all = userService.findAll(emailLike, role, isLocked, cursor, idAfter,
        limit, sortDirection, sortBy);
    return ResponseEntity.status(HttpStatus.OK).body(all);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(@PathVariable Long userId) {
    UserDto user = userService.findById(userId);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PatchMapping(value="/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(@PathVariable Long userId,
      @RequestPart(value = "request") @Valid UserUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile file)
      throws IOException {

    log.info("file: null? {}, empty? {}, name={}, size={}, ct={}",
        file == null,
        file != null && file.isEmpty(),
        file != null ? file.getOriginalFilename() : null,
        file != null ? file.getSize() : null,
        file != null ? file.getContentType() : null
    );

    if (!userId.equals(currentUserId())) {
      throw new AccessDeniedException("본인만 수정할 수 있습니다.");
    }

    UserDto user = userService.updateProfile(currentUserId(), request, file);
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("{userId}/role")
  public ResponseEntity<Void>  updateRole(@PathVariable Long userId, @RequestBody UserRoleUpdateRequest request) {
    userService.updateRole(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("{userId}/locked")
  public ResponseEntity<Void> updateLock(@PathVariable Long userId, @RequestBody UserLockUpdateRequest request) {
    userService.updateLocked(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PatchMapping("{userId}/password")
  public ResponseEntity<Void> changePassword(@PathVariable Long userId, @RequestBody @Valid PasswordChangeRequest request) {
    userService.changePassword(userId, request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || auth.getPrincipal() == null) {
      throw new IllegalStateException("Authentication is null");
    }

    Object user = auth.getPrincipal();

    if (user instanceof MoplUserDetails mud && mud.getUserDto() != null) {
      return mud.getUserDto().id();
    }

    if (user instanceof Long userId) {
      return userId;
    }

    throw new IllegalStateException("Authentication is null");
  }
}

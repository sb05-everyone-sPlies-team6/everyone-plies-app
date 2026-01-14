package team6.finalproject.domain.user.dto;

import java.time.LocalDateTime;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;

public record UserDto(
    Long id,
    LocalDateTime createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    Boolean locked
) {

  public static UserDto from(User user) {
    return new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.getLocked()
    );
  }

}

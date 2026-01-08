package team6.finalproject.domain.user.dto;

import java.time.LocalDateTime;
import team6.finalproject.domain.user.entity.Role;

public record UserDto(
    Long id,
    LocalDateTime createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    Boolean locked
) {

}

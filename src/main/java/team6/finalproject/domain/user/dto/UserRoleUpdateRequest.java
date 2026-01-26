package team6.finalproject.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import team6.finalproject.domain.user.entity.Role;

public record UserRoleUpdateRequest (
    @NotNull(message = "role은 필수입니다.")
    Role role
) {

}

package team6.finalproject.domain.user.dto;

import team6.finalproject.domain.user.entity.Role;

public record UserRoleUpdateRequest (
    Role role
) {

}

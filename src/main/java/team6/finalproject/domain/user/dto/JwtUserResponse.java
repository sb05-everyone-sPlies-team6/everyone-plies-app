package team6.finalproject.domain.user.dto;

import team6.finalproject.domain.user.entity.Role;

public record JwtUserResponse(
        String id,
        String email,
        String name,
        String profileImageUrl,
        Role role
) {
    public static JwtUserResponse from(UserDto userDto) {
        return new JwtUserResponse(
                String.valueOf(userDto.id()),
                userDto.email(),
                userDto.name(),
                userDto.profileImageUrl(),
                userDto.role()
        );
    }
}
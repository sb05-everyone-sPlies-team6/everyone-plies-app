package team6.finalproject.domain.user.dto;

import team6.finalproject.domain.user.entity.User;

public record UserSummary(
        Long userId,
        String name,
        String profileImageUrl
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}

package team6.finalproject.domain.user.dto;

import team6.finalproject.domain.user.entity.User;

public record UserSummary(
        String userId,
        String name,
        String profileImageUrl
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                String.valueOf(user.getId()),
                user.getName(),
                user.getProfileImageUrl()
        );
    }

    public static UserSummary unknown() {
        return new UserSummary("unknown", "unknown", null);
    }
}


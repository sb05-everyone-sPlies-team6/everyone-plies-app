package team6.finalproject.domain.user.dto;

import team6.finalproject.domain.user.entity.User;

public record UserSummary(
        String userId,       // Long
        String name,
        String profileImageUrl
) {
    public static UserSummary from(User user) {
        if (user == null) return null; // 안전하게 null 처리
        return new UserSummary(
                user.getId().toString(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}

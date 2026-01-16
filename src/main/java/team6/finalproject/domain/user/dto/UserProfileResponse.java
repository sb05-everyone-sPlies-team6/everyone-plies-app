package team6.finalproject.domain.user.dto;

public record UserProfileResponse(
        String id,
        String name,
        String email,
        String profileImageUrl
) {
    public static UserProfileResponse from(UserDto userDto) {
        return new UserProfileResponse(
                userDto.idAsString(), // Long → String 변환
                userDto.name(),
                userDto.email(),
                userDto.profileImageUrl()
        );
    }
}

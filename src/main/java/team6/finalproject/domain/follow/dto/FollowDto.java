package team6.finalproject.domain.follow.dto;

public record FollowDto(
    Long id,
    Long followeeId,
    Long followerId
) {

}

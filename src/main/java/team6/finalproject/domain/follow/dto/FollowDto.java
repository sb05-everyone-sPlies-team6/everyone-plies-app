package team6.finalproject.domain.follow.dto;

import team6.finalproject.domain.follow.entity.Follow;

public record FollowDto(
    Long id,
    Long followeeId,
    Long followerId
) {
  public static FollowDto from(Follow follow) {
    return new FollowDto(
        follow.getId(),
        follow.getFollowee().getId(),
        follow.getFollower().getId()
    );
  }
}

package team6.finalproject.domain.follow.dto;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
    @NotNull
    Long followeeId
) {

}

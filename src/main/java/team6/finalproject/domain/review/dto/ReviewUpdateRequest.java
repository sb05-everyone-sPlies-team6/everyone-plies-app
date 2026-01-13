package team6.finalproject.domain.review.dto;

public record ReviewUpdateRequest(
        String text,
        Float rating
) {
}

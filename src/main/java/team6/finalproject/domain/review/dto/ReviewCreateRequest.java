package team6.finalproject.domain.review.dto;

public record ReviewCreateRequest(
        Long contentId,
        String text,
        Float rating
) {
}

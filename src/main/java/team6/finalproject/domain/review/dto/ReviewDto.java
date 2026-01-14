package team6.finalproject.domain.review.dto;

import team6.finalproject.domain.review.entity.Review;
import team6.finalproject.domain.user.dto.UserSummary;

import java.util.List;

public record ReviewDto(
        String id,
        String contentId,
        UserSummary author,
        String text,
        Float rating
) {
    public static ReviewDto from(Review review) {
    return new ReviewDto(
            String.valueOf(review.getId()),
            String.valueOf(review.getContent().getContentId()),
            UserSummary.from(review.getAuthor()), // UserSummaryMapper 대신
            review.getText(),
            review.getRating()
    );
}
    public static List<ReviewDto> fromList(List<Review> reviews) {
        return reviews.stream()
                .map(ReviewDto::from)
                .toList();
    }
}

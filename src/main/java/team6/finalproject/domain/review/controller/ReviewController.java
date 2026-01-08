package team6.finalproject.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team6.finalproject.domain.review.dto.ReviewCreateRequest;
import team6.finalproject.domain.review.dto.ReviewDto;
import team6.finalproject.domain.review.dto.ReviewListResponse;
import team6.finalproject.domain.review.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/reviews?userId=1
    @PostMapping
    public ReviewDto createReview(
            @RequestParam Long userId,   // 임시 userId, TODO: jwt개발되면 수정
            @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.createReview(userId, request);
    }

    @GetMapping
    public ReviewListResponse getReviews(
            @RequestParam Long contentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESCENDING") String sortDirection
    ) {
        return reviewService.getReviewsByContentWithCursor(
                contentId, cursor, limit, sortBy, sortDirection
        );
    }
}

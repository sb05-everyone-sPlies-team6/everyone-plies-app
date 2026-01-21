package team6.finalproject.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team6.finalproject.domain.review.dto.ReviewCreateRequest;
import team6.finalproject.domain.review.dto.ReviewDto;
import team6.finalproject.domain.review.dto.ReviewListResponse;
import team6.finalproject.domain.review.dto.ReviewUpdateRequest;
import team6.finalproject.domain.review.service.ReviewService;
import team6.finalproject.global.security.jwt.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/reviews?userId=1
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReviewCreateRequest request
    ) {
        ReviewDto reviewDto =
                reviewService.createReview(userDetails.getUserDto().id(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewDto);
    }

    @GetMapping
    public ResponseEntity<ReviewListResponse> getReviews(
            @RequestParam Long contentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESCENDING") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserDto().id() : null;

        ReviewListResponse reviewListResponse = reviewService.getReviewsByContentWithCursor(
                contentId, cursor, limit, sortBy, sortDirection, userId);

        return ResponseEntity.ok(reviewListResponse);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReviewDto updatedReview = reviewService.updateReview(
                reviewId,
                userDetails.getUserDto().id(),
                request
        );
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUserDto().id());
        return ResponseEntity.ok().build();
    }
}

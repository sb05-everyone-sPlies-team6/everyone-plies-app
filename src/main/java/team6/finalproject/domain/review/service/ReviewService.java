package team6.finalproject.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.review.dto.ReviewCreateRequest;
import team6.finalproject.domain.review.dto.ReviewDto;
import team6.finalproject.domain.review.dto.ReviewListResponse;
import team6.finalproject.domain.review.dto.ReviewUpdateRequest;
import team6.finalproject.domain.review.entity.Review;
import team6.finalproject.domain.review.repository.ReviewRepository;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Transactional
    public ReviewDto createReview(Long userId, ReviewCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Content content = contentRepository.findById(request.contentId())
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠 없음"));

        if (reviewRepository.existsByAuthorIdAndContentContentId(userId, content.getContentId())) {
            throw new IllegalStateException("이미 리뷰를 작성했습니다");
        }

        Review review = Review.builder()
                .author(user)
                .content(content)
                .rating(request.rating())
                .text(request.text())
                .build();

        reviewRepository.saveAndFlush(review);
        // ✅ 리뷰 작성 후 Content 평균 평점 갱신
        content.addReviewRating(request.rating());

        return ReviewDto.from(review);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByContentWithCursor(
            Long contentId,
            Long cursor,
            int limit,
            String sortBy,
            String sortDirection,
            Long viewerUserId
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASCENDING") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(0, limit + 1, sort); // +1 해서 hasNext 확인

        List<Review> reviews = reviewRepository.findByContentWithCursor(contentId, cursor, pageable);

        boolean hasNext = reviews.size() > limit;
        if (hasNext) {
            reviews = reviews.subList(0, limit);
        }

        String nextCursor = hasNext ? String.valueOf(reviews.get(reviews.size() - 1).getId()) : null;
        
        long totalCount = reviewRepository.countByContentContentId(contentId);

        return new ReviewListResponse(
                ReviewDto.fromList(reviews),
                nextCursor,
                nextCursor, // nextIdAfter 용도로 동일하게 사용
                hasNext,
                totalCount,
                sortBy,
                sortDirection
        );
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if(!review.getAuthor().getId().equals(userId)){
            throw new IllegalStateException("리뷰 작성자만 수정할 수 있습니다.");
        }

        review.setText(request.text());
        review.setRating(request.rating());

        return ReviewDto.from(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        if(!review.getAuthor().getId().equals(userId)){
            throw new IllegalStateException("리뷰 작성자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }
}

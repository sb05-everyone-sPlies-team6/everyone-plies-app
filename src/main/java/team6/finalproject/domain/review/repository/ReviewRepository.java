package team6.finalproject.domain.review.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team6.finalproject.domain.review.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.content.contentId = :contentId AND (:cursor IS NULL OR r.id < :cursor)")
    List<Review> findByContentWithCursor(@Param("contentId") Long contentId,
                                         @Param("cursor") Long cursor,
                                         Pageable pageable);

    boolean existsByAuthorIdAndContentContentId(Long authorId, Long contentId);

    long countByContentContentId(Long contentId);
}

package team6.finalproject.domain.content.dto;

import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;

import java.util.Collections;
import java.util.List;

public record ContentSummary(
        Long id,
        String type,
        String title,
        String description,
        String thumbnailUrl,
        List<String> tags,
        double averageRating,
        int reviewCount
) {

    public static ContentSummary from(Content content) {
        return new ContentSummary(
                content.getContentId(),           // DB PK
                content.getType() != null ? content.getType().name() : null,
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailUrl(),
                Collections.emptyList(),         // tags는 아직 없으니 빈 리스트
                content.getTotalRating() != null ? content.getTotalRating() : 0.0,
                content.getTotalReviews() != null ? content.getTotalReviews() : 0
        );
    }

    // 최소 정보만 필요한 경우
    public static ContentSummary fromId(Long contentId) {
        return new ContentSummary(
                contentId,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                0.0,
                0
        );
    }
}

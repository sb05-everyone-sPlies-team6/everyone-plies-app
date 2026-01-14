package team6.finalproject.domain.content.dto;

import team6.finalproject.domain.content.entity.content.Content;

import java.util.Collections;
import java.util.List;

public record ContentSummary(
        String id,
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
                String.valueOf(content.getContentId()),
                content.getType().name(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailUrl(),
                Collections.emptyList(), // 태그는 필요 시 content에서 가져와 수정
                content.getTotalRating() != null ? content.getTotalRating() : 0.0,
                content.getTotalReviews() != null ? content.getTotalReviews() : 0
        );
    }
}
package team6.finalproject.domain.content.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import team6.finalproject.domain.content.entity.content.Content;

@Getter
@Builder
@AllArgsConstructor
public class ContentResponse {
	private String id; // 명세서가 UUID 형태이므로 String으로 반환 (내부적으론 Long.toString())
	private String type; // movie, drama, sports 등
	private String title;
	private String description;
	private String thumbnailUrl;
	private List<String> tags; // 태그 이름 리스트
	private Double averageRating;
	private Integer reviewCount;
	private Integer watcherCount;

	public static ContentResponse from(Content entity, List<String> tagNames) {
		return ContentResponse.builder()
			.id(entity.getContentId().toString())
			.type(entity.getType().name().toLowerCase())
			.title(entity.getTitle())
			.description(entity.getDescription())
			.thumbnailUrl(entity.getThumbnailUrl())
			.tags(tagNames)
			.averageRating(entity.getTotalRating() != null ? entity.getTotalRating().doubleValue() : 0.0)
			.reviewCount(entity.getTotalReviews() != null ? entity.getTotalReviews() : 0)
			.watcherCount(0) // 실시간 기능 구현 전까지 0으로 세팅
			.build();
	}
}
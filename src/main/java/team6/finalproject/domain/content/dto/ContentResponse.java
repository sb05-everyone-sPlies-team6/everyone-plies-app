package team6.finalproject.domain.content.dto;

import java.util.List;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;

public record ContentResponse(
	String id,
	String type,
	String title,
	String description,
	String thumbnailUrl,
	List<String> tags,
	Double averageRating,
	Integer reviewCount,
	Integer watcherCount
) {
	public static ContentResponse from(Content content, List<String> tags) {
		return new ContentResponse(
			content.getContentId().toString(),
			convertType(content.getType()), // MOVIE -> movie 변환 로직
			content.getTitle(),
			content.getDescription(),
			content.getThumbnailUrl(),
			tags,
			content.getTotalRating().doubleValue(),
			content.getTotalReviews(),
			0 // watcherCount 초기값
		);
	}

	private static String convertType(ContentType type) {
		return switch (type) {
			case MOVIE -> "movie";
			case DRAMA -> "tvSeries";
			case SPORTS -> "sport";
		};
	}
}
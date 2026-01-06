package team6.finalproject.domain.content.dto;

import lombok.Builder;
import lombok.Getter;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;

@Getter
@Builder
public class ContentResponse {
	private Long contentId;
	private String title;
	private ContentType type;
	private String description;
	private String thumbnailUrl;

	public static ContentResponse from(Content content) {
		return ContentResponse.builder()
			.contentId(content.getContentId())
			.title(content.getTitle())
			.type(content.getType())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.build();
	}
}

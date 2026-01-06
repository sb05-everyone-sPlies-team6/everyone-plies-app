package team6.finalproject.domain.content.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "content_id")
	private Long contentId;

	@Column(nullable = false, length = 255)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ContentType type;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;

	@Column(name = "external_id", length = 100)
	private String externalId;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", length = 20)
	private SourceType sourceType;

	@Column(name = "total_rating")
	private Float totalRating = 0.0f;

	@Column(name = "total_reviews")
	private Integer totalReviews = 0;

	@Builder
	public Content(String title, ContentType type, String description, String thumbnailUrl,
		String externalId, SourceType sourceType) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
		this.externalId = externalId;
		this.sourceType = sourceType;
	}

	// 관리자 수정을 위한 메서드
	public void update(String title, ContentType type, String description, String thumbnailUrl) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
	}

	// 2. PATCH 처리를 위한 개별 업데이트 메서드 (인식되지 않던 문제 해결)
	public void updateTitle(String title) {
		this.title = title;
	}

	public void updateType(ContentType type) {
		this.type = type;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
}
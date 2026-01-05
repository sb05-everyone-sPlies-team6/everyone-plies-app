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

	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentType type;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;

	@Column(name = "external_id", nullable = false, length = 100)
	private String externalId;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", length = 20)
	private SourceType sourceType;

	@Column(name = "total_rating")
	private Float totalRating;

	@Column(name = "total_reviews")
	private Integer totalReviews;

	@Builder
	public Content(String title, ContentType type, String description, String thumbnailUrl,
		String externalId, SourceType sourceType) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
		this.externalId = externalId;
		this.sourceType = sourceType;
		this.totalRating = 0.0f;
		this.totalReviews = 0;
	}

	// 관리자 수정을 위한 메서드
	public void update(String title, ContentType type, String description, String thumbnailUrl) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
	}
}
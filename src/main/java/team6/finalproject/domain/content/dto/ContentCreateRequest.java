package team6.finalproject.domain.content.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.finalproject.domain.content.entity.content.ContentType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자가 필요합니다.
public class ContentCreateRequest {
	private String id;
	private String type;         // "movie", "tvSeries", "sport"

	@NotBlank(message = "제목은 필수입니다.")
	private String title;

	@NotBlank(message = "설명은 필수입니다.")
	private String description;
	private String thumbnailUrl; // POST는 thumbnailUrl
	private List<String> tags;
}

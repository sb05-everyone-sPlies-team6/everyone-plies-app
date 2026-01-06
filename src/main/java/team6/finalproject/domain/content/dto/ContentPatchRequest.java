package team6.finalproject.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.finalproject.domain.content.entity.content.ContentType;

@Getter
@NoArgsConstructor
public class ContentPatchRequest {

	@NotBlank(message = "수정할 제목을 입력해주세요.")
	private String title;

	@NotNull(message = "콘텐츠 타입을 선택해주세요.")
	private ContentType type;

	private String description;

	private String thumbnailUrl;
}

package team6.finalproject.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.finalproject.domain.content.entity.content.ContentType;

@Getter// <--- DTO에도 반드시 필요합니다.
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자가 필요합니다.
@AllArgsConstructor // 모든 필드를 포함한 생성자
public class ContentCreateRequest {
	@NotBlank(message = "제목은 필수입니다.")
	private String title;
	@NotNull(message = "타입은 필수입니다.")
	private ContentType type;
	private String description;
	private String thumbnailUrl;
	private String externalId; // API 연동 시 수동 입력 가능
}

package team6.finalproject.domain.content.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.finalproject.domain.content.entity.content.ContentType;

@Getter
@NoArgsConstructor
public class ContentPatchRequest {
	private PatchDetail request;
	private String thumbnail;

	@Getter
	@NoArgsConstructor
	public static class PatchDetail {
		private String title;
		private String description;
		private List<String> tags;
	}
}

package team6.finalproject.domain.content.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

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

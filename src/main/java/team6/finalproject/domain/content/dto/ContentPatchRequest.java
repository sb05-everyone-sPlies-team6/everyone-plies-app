package team6.finalproject.domain.content.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentPatchRequest {
	private PatchDetail request; // 중첩 구조
	private String thumbnail;    // PATCH는 thumbnail (Url 없음)

	@Getter @Setter @NoArgsConstructor
	public static class PatchDetail {
		private String title;
		private String description;
		private List<String> tags;
	}
}

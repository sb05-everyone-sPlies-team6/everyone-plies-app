package team6.finalproject.domain.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentListResponse {
	private List<ContentResponse> data;
	private String nextCursor;
	private String nextIdAfter;
	private boolean hasNext;
	private Long totalCount;
	private String sortBy;
	private String sortDirection;
}

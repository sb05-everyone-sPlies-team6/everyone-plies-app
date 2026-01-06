package team6.finalproject.domain.content.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CursorResponse<T> {
	private List<T> content;    // 데이터 목록
	private Long nextCursor;     // 다음 조회를 위한 커서 (마지막 ID)
	private boolean hasNext;     // 다음 페이지 존재 여부
}
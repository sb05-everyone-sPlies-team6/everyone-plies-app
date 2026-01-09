package team6.finalproject.domain.content.dto;

import java.util.List;

public record CursorResponse<T>(
	List<T> data,
	String nextCursor,
	String nextIdAfter,      // 보조 커서 추가
	boolean hasNext,
	long totalCount,         // 전체 개수 추가
	String sortBy,           // 정렬 기준 추가
	String sortDirection     // 정렬 방향 추가
) {}
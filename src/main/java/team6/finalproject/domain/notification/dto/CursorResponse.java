package team6.finalproject.domain.notification.dto;

import java.util.List;

public record CursorResponse<T>(
    List<T> data,
    String nextCursor,
    Long nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {

}

package team6.finalproject.domain.playlist.dto;

import java.util.List;

public record CursorResponsePlaylistDto<T>(
        List<T> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}

package team6.finalproject.domain.review.dto;

import java.util.List;

public record ReviewListResponse(
        List<ReviewDto> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {}

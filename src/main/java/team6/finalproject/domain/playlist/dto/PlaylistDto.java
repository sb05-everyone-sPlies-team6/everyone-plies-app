package team6.finalproject.domain.playlist.dto;

import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.playlist.entity.Playlist;
import team6.finalproject.domain.user.dto.UserSummary;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistDto(
        String id,
        UserSummary owner,
        String title,
        String description,
        LocalDateTime updatedAt,
        int subscriberCount,
        boolean subscribedByMe,
        int contentCount,
        List<ContentSummary> contents
) {
    public static PlaylistDto fromEntity(
            Playlist playlist,
            List<ContentSummary> contentSummaries,
            boolean subscribedByMe // 여기서 구독 여부 받음
    ) {
        return new PlaylistDto(
                String.valueOf(playlist.getId()),
                UserSummary.from(playlist.getOwner()),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getTotalSubscription(),
                subscribedByMe,
                contentSummaries.size(),
                contentSummaries
        );
    }
}

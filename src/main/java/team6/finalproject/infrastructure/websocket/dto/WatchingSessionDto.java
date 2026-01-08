package team6.finalproject.infrastructure.websocket.dto;

import team6.finalproject.domain.user.dto.UserSummary;

import java.time.LocalDateTime;

public record WatchingSessionDto(
        Long id,
        LocalDateTime createdAt,
        UserSummary watcher
//        ContentDto content TODO: ContentDto 추가시
) {
}

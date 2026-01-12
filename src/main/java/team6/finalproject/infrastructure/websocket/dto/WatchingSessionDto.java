package team6.finalproject.infrastructure.websocket.dto;

import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.user.dto.UserSummary;

import java.time.LocalDateTime;

public record WatchingSessionDto(
        String id,
        LocalDateTime createdAt,
        UserSummary watcher,
        ContentSummary content
) {}

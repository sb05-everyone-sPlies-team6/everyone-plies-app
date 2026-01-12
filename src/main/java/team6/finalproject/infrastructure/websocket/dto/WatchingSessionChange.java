package team6.finalproject.infrastructure.websocket.dto;

import team6.finalproject.domain.user.dto.UserSummary;

import java.util.List;

public record WatchingSessionChange(
        ChangeType type,
        WatchingSessionDto watchingSessions,
        long watcherCount
){}


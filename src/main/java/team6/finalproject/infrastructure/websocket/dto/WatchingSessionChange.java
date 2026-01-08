package team6.finalproject.infrastructure.websocket.dto;

public record WatchingSessionChange(
        ChangeType type,
        WatchingSessionDto watchingSession,
        long watcherCount
)
{ }

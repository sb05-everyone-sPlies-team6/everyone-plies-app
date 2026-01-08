package team6.finalproject.infrastructure.websocket.dto;

import team6.finalproject.domain.user.dto.UserSummary;

public record ContentChatDto(
        UserSummary sender,
        String content
) { }

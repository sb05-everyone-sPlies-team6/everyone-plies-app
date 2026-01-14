package team6.finalproject.domain.dm.dto;

import java.time.LocalDateTime;

public record MessageResponse(
	Long id,
	Long conversationId,
	LocalDateTime createdAt,
	UserSimpleResponse sender,
	UserSimpleResponse receiver,
	String content,
	boolean isRead
) {}
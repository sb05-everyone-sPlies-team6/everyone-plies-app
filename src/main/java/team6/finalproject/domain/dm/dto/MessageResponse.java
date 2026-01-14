package team6.finalproject.domain.dm.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record MessageResponse(
	Long id,
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Long conversationId,
	LocalDateTime createdAt,
	UserSimpleResponse sender,
	UserSimpleResponse receiver,
	String content,
	boolean isRead
) {}
package team6.finalproject.domain.dm.dto;

public record DmResponse(
	Long id,
	UserSimpleResponse with,
	MessageResponse lastestMessage,
	boolean hasUnread
) {}

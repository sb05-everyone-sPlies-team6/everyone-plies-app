package team6.finalproject.domain.dm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DmResponse(
	Long id,
	UserSimpleResponse with,
	@JsonProperty("lastestMessage")
	MessageResponse lastestMessage,
	boolean hasUnread
) {}

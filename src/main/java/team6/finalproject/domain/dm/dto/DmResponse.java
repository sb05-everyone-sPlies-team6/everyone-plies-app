package team6.finalproject.domain.dm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DmResponse(
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Long id,
	UserSimpleResponse with,
	@JsonProperty("lastestMessage")
	MessageResponse lastestMessage,
	boolean hasUnread
) {}

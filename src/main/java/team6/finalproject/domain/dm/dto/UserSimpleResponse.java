package team6.finalproject.domain.dm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UserSimpleResponse(
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Long userId,
	String name,
	String profileImageUrl
) {}

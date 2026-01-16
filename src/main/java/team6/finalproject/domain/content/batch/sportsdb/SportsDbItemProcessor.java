package team6.finalproject.domain.content.batch.sportsdb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import team6.finalproject.domain.content.api.SportsDbEventResponse;
import team6.finalproject.domain.content.batch.ContentBatchDto;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.SourceType;

@Component
public class SportsDbItemProcessor implements ItemProcessor<SportsDbEventResponse.EventDto, ContentBatchDto> {

	@Override
	public ContentBatchDto process(SportsDbEventResponse.EventDto dto) {
		Content content = Content.builder()
			.title(dto.getStrEvent())
			.type(ContentType.SPORTS)
			.description(dto.getStrFilename())
			.thumbnailUrl(dto.getStrThumb())
			.externalId(dto.getIdEvent())
			.sourceType(SourceType.THE_SPORTS_DB)
			.build();

		List<String> tags = new ArrayList<>();
		tags.add(translateSport(dto.getStrSport()));
		tags.add(dto.getStrVenue() != null ? dto.getStrVenue() : "경기장 정보 없음");

		return new ContentBatchDto(content, tags);
	}

	// 종목명 한글 변환 유틸리티
	private String translateSport(String sport) {
		if (sport == null) return "기타";
		return switch (sport.toLowerCase()) {
			case "soccer" -> "축구";
			case "basketball" -> "농구";
			case "baseball" -> "야구";
			case "volleyball" -> "배구";
			default -> sport;
		};
	}
}
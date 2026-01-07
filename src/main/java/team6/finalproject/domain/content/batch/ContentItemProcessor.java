package team6.finalproject.domain.content.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.SourceType;

@Component
public class ContentItemProcessor implements ItemProcessor<TmdbMovieDto, Content> {

	@Override
	public Content process(TmdbMovieDto dto) {
		// TMDB 영화 데이터를 Content 엔티티로 매핑
		return Content.builder()
			.title(dto.getTitle())
			.type(ContentType.MOVIE) // TMDB 영화 API 호출 시
			.description(dto.getDescription())
			.thumbnailUrl("https://image.tmdb.org/t/p/w500" + dto.getPosterPath())
			.externalId(dto.getId().toString())
			.sourceType(SourceType.TMDB)
			.build();
	}
}
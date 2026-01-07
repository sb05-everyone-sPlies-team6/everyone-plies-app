package team6.finalproject.domain.content.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.SourceType;

@Component
public class ContentItemProcessor implements ItemProcessor<TmdbMovieDto, ContentBatchDto> {

	@Override
	public ContentBatchDto process(TmdbMovieDto dto) {
		Content content = Content.builder()
			.title(dto.getTitle())
			.type(ContentType.MOVIE)
			.description(dto.getDescription())
			.thumbnailUrl("https://image.tmdb.org/t/p/w500" + dto.getPosterPath())
			.externalId(dto.getId().toString())
			.sourceType(SourceType.TMDB)
			.build();

		// Writer에서 genreIds가 필요하므로 DTO에 담아서 넘겨줘야 합니다.
		return new ContentBatchDto(content, dto.getGenreIds());
	}
}
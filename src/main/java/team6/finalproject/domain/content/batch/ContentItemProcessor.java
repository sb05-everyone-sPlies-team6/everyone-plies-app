package team6.finalproject.domain.content.batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.SourceType;

@Component
@StepScope
public class ContentItemProcessor implements ItemProcessor<TmdbMovieDto, ContentBatchDto> {
	@Value("#{jobParameters['contentType']}")
	private String contentTypeParam;

	@Override
	public ContentBatchDto process(TmdbMovieDto dto) {
		// getEffectiveTitle()을 사용하여 title 혹은 name을 가져옴
		String typeStr = (contentTypeParam != null) ? contentTypeParam : "MOVIE";

		Content content = Content.builder()
			.title(dto.getEffectiveTitle())
			.type(ContentType.valueOf(contentTypeParam)) // MOVIE 또는 DRAMA 할당
			.description(dto.getDescription())
			.thumbnailUrl("https://image.tmdb.org/t/p/w500" + dto.getPosterPath())
			.externalId(dto.getId().toString())
			.sourceType(SourceType.TMDB)
			.build();

		return new ContentBatchDto(content, dto.getGenreIds());
	}
}
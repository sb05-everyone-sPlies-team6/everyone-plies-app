package team6.finalproject.global.config;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.api.SportsDbEventResponse;
import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.batch.ContentBatchDto;
import team6.finalproject.domain.content.batch.sportsdb.SportsDbItemProcessor;
import team6.finalproject.domain.content.batch.sportsdb.SportsDbItemReader;
import team6.finalproject.domain.content.batch.tmdb.TmDbContentItemProcessor;
import team6.finalproject.domain.content.batch.ContentItemWriter;
import team6.finalproject.domain.content.batch.tmdb.TmdbItemReader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ContentImportJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final TmdbItemReader tmdbItemReader;
	private final TmDbContentItemProcessor tmDbContentItemProcessor;
	private final ContentItemWriter contentItemWriter;
	private final SportsDbItemReader sportsDbItemReader;
	private final SportsDbItemProcessor sportsDbItemProcessor;

	@Bean
	public Job contentImportJob() {
		return new JobBuilder("contentImportJob", jobRepository)
			.start(tmdbMovieStep())
			.next(tmdbTvStep())
			.next(sportsDbStep())
			//.start(sportsDbStep())
			.build();
	}

	@Bean
	public Step tmdbMovieStep() {
		return new StepBuilder("tmdbMovieStep", jobRepository)
			.<TmdbMovieDto, ContentBatchDto>chunk(10, transactionManager) // 타입을 ContentBatchDto로 지정
			.reader(tmdbItemReader)
			.processor(tmDbContentItemProcessor)
			.writer(contentItemWriter)
			.build();
	}

	@Bean
	public Step tmdbTvStep() {
		return new StepBuilder("tmdbTvStep", jobRepository)
			.<TmdbMovieDto, ContentBatchDto>chunk(10, transactionManager)
			.reader(tmdbItemReader) // 동일한 리더 사용 (파라미터로 구분)
			.processor(tmDbContentItemProcessor)
			.writer(contentItemWriter)
			.build();
	}

	@Bean
	public Step sportsDbStep() {
		return new StepBuilder("sportsDbStep", jobRepository)
			.<SportsDbEventResponse.EventDto, ContentBatchDto>chunk(10, transactionManager)
			.reader(sportsDbItemReader)
			.processor(sportsDbItemProcessor)
			.writer(contentItemWriter) // 기존 Writer 재사용 가능
			.build();
	}
}
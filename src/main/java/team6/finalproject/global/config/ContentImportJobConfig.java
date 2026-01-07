package team6.finalproject.global.config;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.batch.ContentItemProcessor;
import team6.finalproject.domain.content.batch.ContentItemWriter;
import team6.finalproject.domain.content.batch.TmdbItemReader;
import team6.finalproject.domain.content.entity.content.Content;

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
	private final ContentItemProcessor contentItemProcessor;
	private final ContentItemWriter contentItemWriter;

	@Bean
	public Job contentImportJob() {
		return new JobBuilder("contentImportJob", jobRepository)
			.start(tmdbMovieStep())
			.build();
	}

	@Bean
	public Step tmdbMovieStep() {
		return new StepBuilder("tmdbMovieStep", jobRepository)
			.<TmdbMovieDto, Content>chunk(10, transactionManager) // 10개 단위로 처리
			.reader(tmdbItemReader)
			.processor(contentItemProcessor)
			.writer(contentItemWriter)
			.build();
	}
}
package team6.finalproject.domain.content.batch.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team6.finalproject.domain.content.api.TmdbFeignClient;
import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.api.TmdbMovieResponse;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope // 이제 JobParameter를 인식할 수 있습니다.
@RequiredArgsConstructor
public class TmdbItemReader implements ItemStreamReader<TmdbMovieDto> {

	private final TmdbFeignClient tmdbFeignClient;

	@Value("${tmdb.api.key}")
	private String apiKey;

	// JobParameters에서 contentType을 받아옵니다 (MOVIE 또는 DRAMA)
	@Value("#{jobParameters['contentType']}")
	private String contentTypeParam;

	private int nextApiPage = 1;
	private List<TmdbMovieDto> resultsBuffer = new ArrayList<>();
	private int totalPages = 1;
	private long processedCount = 0; // 실제 반환된 데이터 개수 카운트

	@Override
	public TmdbMovieDto read() {

		//버퍼가 비어있으면 다음 페이지 호출
		if (resultsBuffer.isEmpty()) {
			if (nextApiPage > totalPages) {
				return null;
			}
			fetchNextPage();
		}

		//버퍼에서 하나 꺼내면서 카운트 증가
		if (!resultsBuffer.isEmpty()) {
			processedCount++;
			return resultsBuffer.remove(0);
		}

		return null;
	}

	@Override
	public void open(ExecutionContext executionContext) {
		this.resultsBuffer.clear();
		this.nextApiPage = 1;
		this.processedCount = 0;
		log.info("### {} Reader 초기화 완료 ###", contentTypeParam);
	}

	private void fetchNextPage() {
		//log.info("### TMDB API 호출 (타입: {}) - Page: {} ###", contentTypeParam, nextApiPage);

		try {
			TmdbMovieResponse response;

			// 파라미터 값에 따라 호출할 Feign Client 메서드를 분기합니다.
			if ("DRAMA".equals(contentTypeParam)) {
				// 드라마(TV 시리즈) API 호출
				response = tmdbFeignClient.discoverTvShows(
					apiKey,
					"ko-KR",
					nextApiPage,
					"popularity.desc"
				);
			} else {
				// 영화 API 호출
				response = tmdbFeignClient.discoverMovies(
					apiKey,
					"ko-KR",
					nextApiPage,
					"popularity.desc"
				);
			}

			if (response != null && response.getResults() != null) {
				this.resultsBuffer.addAll(response.getResults());
				this.totalPages = response.getTotalPages();
				this.nextApiPage++;
			}
		} catch (Exception e) {
			log.error("TMDB API 호출 중 오류 발생: {}", e.getMessage());
			this.totalPages = 0;
		}
	}
}
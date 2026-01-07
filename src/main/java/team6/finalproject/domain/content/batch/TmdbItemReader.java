package team6.finalproject.domain.content.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team6.finalproject.domain.content.api.TmdbFeignClient;
import team6.finalproject.domain.content.api.TmdbMovieDto;
import team6.finalproject.domain.content.api.TmdbMovieResponse;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbItemReader implements ItemReader<TmdbMovieDto> {

	private final TmdbFeignClient tmdbFeignClient;

	@Value("${tmdb.api.key}")
	private String apiKey;

	private int nextApiPage = 1; // 다음에 호출할 API 페이지
	private List<TmdbMovieDto> resultsBuffer = new ArrayList<>(); // 현재 페이지의 데이터 버퍼
	private int totalPages = 1; // API가 제공하는 전체 페이지 수

	@Override
	public TmdbMovieDto read() {
		// 1. 버퍼가 비어있으면 다음 페이지 데이터를 가져옴
		if (resultsBuffer.isEmpty()) {
			if (nextApiPage > totalPages) {
				return null; // 모든 데이터를 읽었으면 종료
			}
			fetchNextPage();
		}

		// 2. 버퍼에서 데이터를 하나씩 꺼내서 반환
		return resultsBuffer.isEmpty() ? null : resultsBuffer.remove(0);
	}

	private void fetchNextPage() {
		log.info("### TMDB API 호출 - Page: {} ###", nextApiPage);

		try {
			TmdbMovieResponse response = tmdbFeignClient.discoverMovies(
				apiKey,
				"ko-KR", // 한국어 데이터
				nextApiPage,
				"popularity.desc" // 인기순 정렬
			);

			if (response != null && response.getResults() != null) {
				this.resultsBuffer.addAll(response.getResults());
				this.totalPages = response.getTotalPages();
				this.nextApiPage++;
			}
		} catch (Exception e) {
			log.error("TMDB API 호출 중 오류 발생: {}", e.getMessage());
			// 에러 시 다음 페이지로 넘어가거나 종료하는 전략 선택 가능
			this.nextApiPage = Integer.MAX_VALUE;
		}
	}
}
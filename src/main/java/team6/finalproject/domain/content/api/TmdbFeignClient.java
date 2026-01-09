package team6.finalproject.domain.content.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tmdbClient", url = "https://api.themoviedb.org/3")
public interface TmdbFeignClient {

	// 발견하기(Discover) API: 다양한 필터를 걸어 영화 목록을 가져옵니다.
	@GetMapping("/discover/movie")
	TmdbMovieResponse discoverMovies(
		@RequestParam("api_key") String apiKey,
		@RequestParam("language") String language,
		@RequestParam("page") int page,
		@RequestParam("sort_by") String sortBy
	);

	// TV(드라마) 목록 가져오기
	@GetMapping("/discover/tv")
	TmdbMovieResponse discoverTvShows(
		@RequestParam("api_key") String apiKey,
		@RequestParam("language") String language,
		@RequestParam("page") int page,
		@RequestParam("sort_by") String sortBy
	);
}
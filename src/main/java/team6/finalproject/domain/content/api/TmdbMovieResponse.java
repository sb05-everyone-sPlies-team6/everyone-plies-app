package team6.finalproject.domain.content.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TmdbMovieResponse {
	private int page;
	private List<TmdbMovieDto> results; // 영화 목록
	@JsonProperty("total_pages")
	private int totalPages;
	@JsonProperty("total_results")
	private int totalResults;
}
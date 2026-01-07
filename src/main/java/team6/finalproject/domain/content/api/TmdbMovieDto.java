package team6.finalproject.domain.content.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TmdbMovieDto {
	private Long id;              // TMDB의 고유 ID (external_id로 저장됨)
	private String title;         // 제목
	@JsonProperty("overview")
	private String description;   // 상세 설명
	@JsonProperty("poster_path")
	private String posterPath;    // 썸네일 경로
	@JsonProperty("genre_ids")
	private List<Integer> genreIds; // 장르 ID (태그로 변환 예정)
	@JsonProperty("vote_average")
	private Float voteAverage;    // 평점
}
package team6.finalproject.domain.content.batch.tmdb;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TmdbGenreMapper {
	private static final Map<Integer, String> genreMap = Map.ofEntries(
		Map.entry(28, "액션"), Map.entry(12, "모험"), Map.entry(16, "애니메이션"),
		Map.entry(35, "코미디"), Map.entry(80, "범죄"), Map.entry(99, "다큐멘터리"),
		Map.entry(18, "드라마"), Map.entry(10751, "가족"), Map.entry(14, "판타지"),
		Map.entry(36, "역사"), Map.entry(27, "공포"), Map.entry(10402, "음악"),
		Map.entry(9648, "미스터리"), Map.entry(10749, "로맨스"), Map.entry(878, "SF"),
		Map.entry(10770, "TV 영화"), Map.entry(53, "스릴러"), Map.entry(10752, "전쟁"),
		Map.entry(37, "서부")
	);

	public static List<String> toGenreNames(List<Integer> genreIds) {
		if (genreIds == null) return Collections.emptyList();
		return genreIds.stream()
			.map(id -> genreMap.getOrDefault(id, "기타"))
			.toList();
	}
}

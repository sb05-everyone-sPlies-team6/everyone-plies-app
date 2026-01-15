package team6.finalproject.domain.content.repository;

import java.util.List;
import java.util.UUID;

import team6.finalproject.domain.content.entity.content.Content;

public interface ContentRepositoryCustom {
	List<Content> findAllByCursor(
		String cursor,
		UUID idAfter,
		int limit,
		List<String> tagsIn,
		String sortBy,
		String sortDirection,
		String typeEqual,
		String keywordLike
	);
}
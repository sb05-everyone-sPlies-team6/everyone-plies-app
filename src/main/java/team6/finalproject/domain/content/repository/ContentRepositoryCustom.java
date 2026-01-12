package team6.finalproject.domain.content.repository;

import java.util.List;

import team6.finalproject.domain.content.entity.content.Content;

public interface ContentRepositoryCustom {
	List<Content> findAllByCursor(Long cursor, int limit, String sortBy, String sortDirection, String typeEqual, String keywordLike);
}
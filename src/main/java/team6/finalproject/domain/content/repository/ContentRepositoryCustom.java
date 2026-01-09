package team6.finalproject.domain.content.repository;

import java.util.List;

import team6.finalproject.domain.content.entity.content.Content;

// 1. 인터페이스 정의
public interface ContentRepositoryCustom {
	List<Content> findAllByCursor(Long cursor, int size, String sortBy, String sortDirection, String type,
		String keyword);
}
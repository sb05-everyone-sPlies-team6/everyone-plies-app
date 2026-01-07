package team6.finalproject.domain.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.tag.ContentTag;
import team6.finalproject.domain.content.entity.tag.Tag;

public interface ContentTagRepository extends JpaRepository<ContentTag, Long> {
	// 특정 콘텐츠와 태그의 매핑이 이미 존재하는지 확인 (중복 저장 방지)
	boolean existsByContentAndTag(Content content, Tag tag);

	// 특정 콘텐츠에 연결된 모든 매핑 삭제 (수정/삭제 시 필요)
	void deleteByContent(Content content);

	// 특정 콘텐츠에 연결된 모든 매핑 조회
	List<ContentTag> findAllByContent(Content content);
}
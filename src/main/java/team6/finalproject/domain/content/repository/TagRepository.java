package team6.finalproject.domain.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import team6.finalproject.domain.content.entity.tag.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
	// 태그 이름으로 검색 (이미 저장된 태그인지 확인하기 위함)
	Optional<Tag> findByName(String name);
}
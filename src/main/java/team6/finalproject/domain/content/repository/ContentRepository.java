package team6.finalproject.domain.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.SourceType;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, ContentRepositoryCustom {
	Optional<Content> findByExternalIdAndSourceType(String externalId, SourceType sourceType);
}
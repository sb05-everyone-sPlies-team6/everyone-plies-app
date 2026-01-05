package team6.finalproject.domain.content.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.dto.ContentCreateRequest;
import team6.finalproject.domain.content.dto.ContentUpdateRequest;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.SourceType;
import team6.finalproject.domain.content.repository.ContentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

	private final ContentRepository contentRepository;

	@Transactional
	public Long createContent(ContentCreateRequest dto) {
		// 수동 등록 시 externalId가 없으면 UUID 등으로 대체
		String externalId = dto.getExternalId() != null ? dto.getExternalId() : UUID.randomUUID().toString();

		Content content = Content.builder()
			.title(dto.getTitle())
			.type(dto.getType())
			.description(dto.getDescription())
			.thumbnailUrl(dto.getThumbnailUrl())
			.externalId(externalId)
			.sourceType(SourceType.MANUAL)
			.build();

		return contentRepository.save(content).getContentId();
	}

	@Transactional
	public void updateContent(Long id, ContentUpdateRequest dto) {
		Content content = contentRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));

		content.update(dto.getTitle(), dto.getType(), dto.getDescription(), dto.getThumbnailUrl());
	}

	@Transactional
	public void deleteContent(Long id) {
		contentRepository.deleteById(id);
	}
}
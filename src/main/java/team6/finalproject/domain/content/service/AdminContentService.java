package team6.finalproject.domain.content.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.dto.ContentCreateRequest;
import team6.finalproject.domain.content.dto.ContentResponse;
import team6.finalproject.domain.content.dto.ContentPatchRequest;
import team6.finalproject.domain.content.dto.CursorResponse;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.content.SourceType;
import team6.finalproject.domain.content.repository.ContentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

	private final ContentRepository contentRepository;

	// 커서 기반 조회 로직 (QueryDSL 구현 권장)
	public CursorResponse<ContentResponse> getContents(Long cursor, int size) {
		// 1. cursor보다 작은 ID를 size + 1개 조회 (다음 페이지 유무 확인용)
		// 2. 결과를 ContentResponse DTO로 변환
		// 3. nextCursor 계산 및 CursorResponse 반환
		return null; // 상세 QueryDSL 로직은 Repository 영역에서 처리
	}

	public ContentResponse getContent(Long contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다. ID: " + contentId));

		// Entity를 DTO로 변환하여 반환
		return ContentResponse.from(content);
	}

	@Transactional
	public void patchContent(Long contentId, ContentPatchRequest request) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));

		// null 체크를 통해 전달된 데이터만 수정 (Partial Update)
		if (request.getTitle() != null && !request.getTitle().isBlank()) {
			content.updateTitle(request.getTitle());
		}
		if (request.getType() != null) {
			content.updateType(request.getType());
		}
		if (request.getDescription() != null) {
			content.updateDescription(request.getDescription());
		}
		if (request.getThumbnailUrl() != null) {
			content.updateThumbnailUrl(request.getThumbnailUrl());
		}
	}

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
	public void updateContent(Long id, ContentPatchRequest dto) {
		Content content = contentRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("해당 콘텐츠를 찾을 수 없습니다. ID: " + id));

		content.update(
			dto.getTitle(),
			dto.getType(),
			dto.getDescription(),
			dto.getThumbnailUrl()
		);
	}

	@Transactional
	public void deleteContent(Long id) {
		contentRepository.deleteById(id);
	}
}
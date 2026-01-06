package team6.finalproject.domain.content.service;

import java.util.Collections;
import java.util.List;
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
			.orElseThrow(() -> new RuntimeException("Content not found"));

		// 실제로는 content_tags 테이블 조인 필요 (우선 빈 리스트)
		List<String> tags = Collections.emptyList();
		return ContentResponse.from(content, tags);
	}

	@Transactional
	public ContentResponse createContent(ContentCreateRequest dto) {
		Content content = Content.builder()
			.title(dto.getTitle())
			.type(dto.getType())
			.description(dto.getDescription())
			.thumbnailUrl(dto.getThumbnailUrl())
			.externalId(dto.getExternalId() != null ? dto.getExternalId() : UUID.randomUUID().toString())
			.sourceType(SourceType.MANUAL)
			.build();

		Content saved = contentRepository.save(content);
		return ContentResponse.from(saved, dto.getTags());
	}

	@Transactional
	public ContentResponse patchContent(Long contentId, ContentPatchRequest request) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("Content not found"));

		// 중첩 구조(request 필드) 접근
		if (request.getRequest() != null) {
			ContentPatchRequest.PatchDetail detail = request.getRequest();
			if (detail.getTitle() != null) content.updateTitle(detail.getTitle());
			if (detail.getDescription() != null) content.updateDescription(detail.getDescription());
			// 태그 업데이트 로직은 별도의 매핑 테이블 처리가 필요함
		}

		if (request.getThumbnail() != null) {
			content.updateThumbnailUrl(request.getThumbnail());
		}

		return ContentResponse.from(content, Collections.emptyList());
	}

	@Transactional
	public void deleteContent(Long contentId) {
		contentRepository.deleteById(contentId);
	}
}
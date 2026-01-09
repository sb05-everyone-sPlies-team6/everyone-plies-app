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

	public CursorResponse<ContentResponse> getContents(Long cursor, int limit, String sortBy, String sortDirection, String typeEqual, String keywordLike) {

		// 1. 모든 파라미터를 레포지토리에 전달
		List<Content> contents = contentRepository.findAllByCursor(cursor, limit, sortBy, sortDirection, typeEqual, keywordLike);

		boolean hasNext = contents.size() > limit;
		List<Content> resultContents = hasNext ? contents.subList(0, limit) : contents;

		List<ContentResponse> data = resultContents.stream()
			.map(c -> ContentResponse.from(c, Collections.emptyList()))
			.toList();

		String nextCursor = hasNext ? resultContents.get(limit - 1).getContentId().toString() : null;

		// 2. 응답 시 sortBy와 sortDirection도 다시 포함
		return new CursorResponse<>(
			data,
			nextCursor,
			null, // nextIdAfter
			hasNext,
			contentRepository.count(),
			sortBy != null ? sortBy : "createdAt",
			sortDirection != null ? sortDirection : "DESCENDING"
		);
	}

	public ContentResponse getContent(Long contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("Content not found"));

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
			// 태그 업데이트 로직은 별도의 매핑 테이블 처리가 필요
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
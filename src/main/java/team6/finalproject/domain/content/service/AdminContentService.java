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

	@Transactional(readOnly = true)
	public CursorResponse<ContentResponse> getContents(
		Long cursor, int limit, String sortBy, String sortDirection, String type, String keyword) {

		List<Content> contents = contentRepository.findAllByCursor(cursor, limit, sortBy, sortDirection, type, keyword);

		boolean hasNext = contents.size() > limit;
		List<Content> resultContents = hasNext ? contents.subList(0, limit) : contents;

		List<ContentResponse> data = resultContents.stream()
			.map(content -> ContentResponse.from(content, Collections.emptyList())) // 태그는 추후 매핑
			.toList();

		String nextCursor = hasNext ? resultContents.get(limit - 1).getContentId().toString() : null;

		return new CursorResponse<>(
			data,
			nextCursor,
			null, // nextIdAfter (보조 커서)
			hasNext,
			contentRepository.count(), // totalCount
			sortBy,
			sortDirection
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
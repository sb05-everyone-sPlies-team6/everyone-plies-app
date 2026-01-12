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
import team6.finalproject.domain.content.entity.content.ContentType;
import team6.finalproject.domain.content.entity.content.SourceType;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.content.repository.ContentTagRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminContentService {

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;

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

	public ContentResponse createContent(ContentCreateRequest dto) {
		Content content = Content.builder()
			.title(dto.getTitle())
			.type(mapToEnum(dto.getType()))
			.description(dto.getDescription())
			.thumbnailUrl(dto.getThumbnailUrl())
			.externalId(dto.getId() != null ? dto.getId() : java.util.UUID.randomUUID().toString())
			.sourceType(SourceType.MANUAL)
			.build();
		return ContentResponse.from(contentRepository.save(content), dto.getTags());
	}

	@Transactional
	public ContentResponse patchContent(Long contentId, ContentPatchRequest dto) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("Content not found"));
		System.out.println(dto.getRequest().getTitle());

		// 중첩된 request 객체 내부 값 처리
		if (dto.getRequest() != null) {
			ContentPatchRequest.PatchDetail detail = dto.getRequest();
			if (detail.getTitle() != null) content.updateTitle(detail.getTitle());
			if (detail.getDescription() != null) content.updateDescription(detail.getDescription());
		}

		// 평면 구조의 thumbnail 처리
		if (dto.getThumbnail() != null) {
			content.updateThumbnailUrl(dto.getThumbnail());
		}

		return ContentResponse.from(content, Collections.emptyList());
	}

	@Transactional
	public void deleteContent(Long contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("Content not found"));
		contentTagRepository.deleteByContent(content);
		contentRepository.delete(content);
	}

	private ContentType mapToEnum(String type) {
		//null 체크 추가
		if (type == null) {
			return ContentType.MOVIE; // 혹은 적절한 기본값
		}

		return switch (type) {
			case "tvSeries" -> ContentType.DRAMA;
			case "sport" -> ContentType.SPORTS;
			case "movie" -> ContentType.MOVIE;
			default -> ContentType.MOVIE;
		};
	}
}
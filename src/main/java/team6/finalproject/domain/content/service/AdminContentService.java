package team6.finalproject.domain.content.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import team6.finalproject.domain.content.entity.tag.ContentTag;
import team6.finalproject.domain.content.entity.tag.Tag;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.content.repository.ContentTagRepository;
import team6.finalproject.domain.content.repository.TagRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminContentService {

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final TagRepository tagRepository;

	public CursorResponse<ContentResponse> getContents(
		String cursor, java.util.UUID idAfter, int limit, List<String> tagsIn,
		String sortBy, String sortDirection, String typeEqual, String keywordLike) {

		Long cursorLong = null;
		try {
			if (cursor != null && !cursor.isBlank()) {
				cursorLong = Long.parseLong(cursor);
			}
		} catch (NumberFormatException e) {
			cursorLong = null;
		}

		List<Content> contents = contentRepository.findAllByCursor(
			cursor, idAfter, limit, tagsIn, sortBy, sortDirection, typeEqual, keywordLike
		);

		boolean hasNext = contents.size() > limit;
		List<Content> resultContents = hasNext ? contents.subList(0, limit) : contents;

		List<ContentResponse> data = resultContents.stream()
			.map(c -> {
				List<String> tags = getTagNames(c);
				return ContentResponse.from(c, tags);
			})
			.toList();

		//다음 커서 및 보조 커서 설정
		String nextCursor = null;
		String nextIdAfter = null;

		if (hasNext) {
			Content lastItem = resultContents.get(limit - 1);
			nextCursor = lastItem.getContentId().toString();
			nextIdAfter = lastItem.getExternalId(); // externalId가 UUID 문자열인 경우
		}

		return new CursorResponse<>(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			contentRepository.count(),
			sortBy != null ? sortBy : "createdAt",
			sortDirection != null ? sortDirection : "DESCENDING"
		);
	}

	public ContentResponse getContent(Long contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("콘텐츠를 찾을 수 없습니다."));

		return ContentResponse.from(content, getTagNames(content));
	}

	// 태그 저장 공통 로직
	private List<String> saveTags(Content content, List<String> tagNames, String contentType) {
		//기존에 이미 DB에 저장된 태그들 가져옴
		Set<String> finalTagNames = new LinkedHashSet<>(getTagNames(content));

		/*
		// 콘텐츠 타입을 태그로 추가 -> 중복 저장?
		if (contentType != null) {
			finalTagNames.add(mapToFrontendType(content.getType()));
		}
		 */

		//새로운 태그들이 들어온 경우에만 추가
		if (tagNames != null && !tagNames.isEmpty()) {
			for (String name : tagNames) {
				if (name == null || name.isBlank()) continue;
				String[] splitNames = name.split(",");
				for (String splitName : splitNames) {
					finalTagNames.add(splitName.trim());
				}
			}
		}

		// DB 저장
		for (String tagName : finalTagNames) {
			Tag tag = tagRepository.findByName(tagName)
				.orElseGet(() -> tagRepository.save(new Tag(tagName)));

			if (!contentTagRepository.existsByContentAndTag(content, tag)) {
				contentTagRepository.save(new ContentTag(content, tag));
			}
		}

		return new ArrayList<>(finalTagNames);
	}

	// 특정 콘텐츠의 태그 리스트 조회 로직
	private List<String> getTagNames(Content content) {
		return contentTagRepository.findAllByContent(content).stream()
			.map(ct -> ct.getTag().getName())
			.toList();
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

		Content saved = contentRepository.save(content);

		List<String> allTags = saveTags(saved, dto.getTags(), dto.getType());
		return ContentResponse.from(saved, allTags);
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

			if (detail.getTags() != null) {
				contentTagRepository.deleteByContent(content);
				// 새로운 태그 리스트로 다시 저장
				saveTags(content, detail.getTags(), null);
			}
		}

		// 평면 구조의 thumbnail 처리
		if (dto.getThumbnail() != null) {
			content.updateThumbnailUrl(dto.getThumbnail());
		}

		return ContentResponse.from(content, getTagNames(content));
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
package team6.finalproject.domain.content.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.common.S3Folder;
import team6.finalproject.domain.common.S3Service;
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
public class ContentService {

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final TagRepository tagRepository;
	private final S3Service s3Service;

	public CursorResponse<ContentResponse> getContents(
		String cursor, String idAfter, int limit, List<String> tagsIn,
		String sortBy, String sortDirection, String typeEqual, String keywordLike) {

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

		List<String> dbTags = getTagNames(content);
		List<String> displayTags = new ArrayList<>();

		//유형 태그를 첫 번째에 추가
		//displayTags.add(content.getType().getDescription());
		displayTags.addAll(dbTags);

		return ContentResponse.from(content, displayTags);
	}

	public Content getContentById(Long contentId) {
		return contentRepository.findById(contentId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다: " + contentId));
	}

	private void syncTags(Content content, List<String> tagNames) {
		//기존 매핑 삭제
		contentTagRepository.deleteByContent(content);

		if (tagNames == null || tagNames.isEmpty()) return;

		//새로운 태그들만 깔끔하게 저장
		for (String name : tagNames) {
			if (name == null || name.isBlank()) continue;

			// 콤마로 들어올 경우를 대비한 처리 (관리자 수동 입력 대응)
			String[] splitNames = name.split(",");
			for (String splitName : splitNames) {
				String trimmedName = splitName.trim();
				if (trimmedName.isEmpty()) continue;

				Tag tag = tagRepository.findByName(trimmedName)
					.orElseGet(() -> tagRepository.save(new Tag(trimmedName)));

				if (!contentTagRepository.existsByContentAndTag(content, tag)) {
					contentTagRepository.save(new ContentTag(content, tag));
				}
			}
		}
	}

	// 특정 콘텐츠의 태그 리스트 조회 로직
	private List<String> getTagNames(Content content) {
		return contentTagRepository.findAllByContent(content).stream()
			.map(ct -> ct.getTag().getName())
			.toList();
	}

	public ContentResponse createContent(ContentCreateRequest dto, MultipartFile file) {
		String thumbnailUrl = dto.getThumbnailUrl();

		if (file != null && !file.isEmpty()) {
			// CONTENTS 폴더로 업로드
			thumbnailUrl = s3Service.upload(file, S3Folder.CONTENTS.name());
		}

		Content content = Content.builder()
			.title(dto.getTitle())
			.type(mapToEnum(dto.getType()))
			.description(dto.getDescription())
			.thumbnailUrl(thumbnailUrl)
			.externalId(java.util.UUID.randomUUID().toString())
			.sourceType(SourceType.MANUAL)
			.build();

		Content saved = contentRepository.save(content);
		syncTags(saved, dto.getTags());

		return ContentResponse.from(saved, getTagNames(saved));
	}

	@Transactional
	public ContentResponse patchContent(Long contentId, ContentPatchRequest dto, MultipartFile file) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new RuntimeException("Content not found"));

		if (file != null && !file.isEmpty()) {
			String newUrl = s3Service.upload(file, S3Folder.CONTENTS.name());
			content.updateThumbnailUrl(newUrl);
		}

		if (dto.getRequest() != null) {
			ContentPatchRequest.PatchDetail detail = dto.getRequest();
			if (detail.getTitle() != null) content.updateTitle(detail.getTitle());
			if (detail.getDescription() != null) content.updateDescription(detail.getDescription());
			if (detail.getTags() != null) {
				syncTags(content, detail.getTags());
			}
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
package team6.finalproject.domain.content.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.dto.ContentCreateRequest;
import team6.finalproject.domain.content.dto.ContentResponse;
import team6.finalproject.domain.content.dto.ContentPatchRequest;
import team6.finalproject.domain.content.dto.CursorResponse;
import team6.finalproject.domain.content.service.AdminContentService;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class AdminContentController {

	private final AdminContentService adminContentService;

	// 1. 콘텐츠 목록 조회 (커서 페이지네이션)
	@GetMapping
	public ResponseEntity<CursorResponse<ContentResponse>> getContents(
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int limit,
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String sortDirection,
		@RequestParam(required = false) String typeEqual,
		@RequestParam(required = false) String keywordLike) {

		return ResponseEntity.ok(adminContentService.getContents(
			cursor, limit, sortBy, sortDirection, typeEqual, keywordLike
		));
	}
	// 2. 콘텐츠 생성 (어드민)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentResponse> createContent(
		@RequestParam("title") String title,
		@RequestParam("type") String type,
		@RequestParam(value = "id", required = false) String id,
		@RequestParam(value = "description", required = false) String description,
		@RequestParam(value = "thumbnailUrl", required = false) String thumbnailUrl,
		@RequestParam(value = "tags", required = false) List<String> tags) {

		ContentCreateRequest request = new ContentCreateRequest(id, type, title, description, thumbnailUrl, tags);
		return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createContent(request));
	}

	// 3. 콘텐츠 단건 조회
	@GetMapping("/{contentId}")
	public ResponseEntity<ContentResponse> getContent(@PathVariable Long contentId) {
		return ResponseEntity.ok(adminContentService.getContent(contentId));
	}

	// 4. 어드민 콘텐츠 수정 (PATCH)
	@PatchMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentResponse> patchContent(
		@PathVariable Long contentId,
		@RequestPart("request") ContentPatchRequest.PatchDetail request, // JSON 파트 처리
		@RequestPart(value = "thumbnail", required = false) String thumbnail) {

		// 프론트 명세대로 request 객체와 thumbnail 스트링을 따로 받습니다.
		ContentPatchRequest patchRequest = new ContentPatchRequest();
		patchRequest.setRequest(request);
		patchRequest.setThumbnail(thumbnail);

		return ResponseEntity.ok(adminContentService.patchContent(contentId, patchRequest));
	}

	// 5. 어드민 콘텐츠 삭제
	@DeleteMapping("/{contentId}")
	public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
		adminContentService.deleteContent(contentId);
		return ResponseEntity.ok().build(); //200성공
	}
}
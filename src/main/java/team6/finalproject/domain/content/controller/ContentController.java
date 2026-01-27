package team6.finalproject.domain.content.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.dto.ContentCreateRequest;
import team6.finalproject.domain.content.dto.ContentResponse;
import team6.finalproject.domain.content.dto.ContentPatchRequest;
import team6.finalproject.domain.content.dto.CursorResponse;
import team6.finalproject.domain.content.service.ContentService;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

	private final ContentService contentService;

	//콘텐츠 목록 조회 (커서 페이지네이션)
	@GetMapping
	public ResponseEntity<CursorResponse<ContentResponse>> getContents(
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) String idAfter,
		@RequestParam(defaultValue = "10") int limit,
		@RequestParam(required = false) List<String> tagsIn,
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String sortDirection,
		@RequestParam(required = false) String typeEqual,
		@RequestParam(required = false) String keywordLike,
		@RequestParam(required = false) String sourceEqual) {

		return ResponseEntity.ok(contentService.getContents(
			cursor, idAfter, limit, tagsIn, sortBy, sortDirection, typeEqual, keywordLike, sourceEqual
		));
	}
	//콘텐츠 생성 (어드민)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentResponse> createContent(
		@RequestPart("request") @Valid ContentCreateRequest request,
		@RequestPart(value = "thumbnail", required = false) MultipartFile file) {

		// 서비스 메서드 규격에 맞춰 request와 file 전달
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(contentService.createContent(request, file));
	}

	//콘텐츠 단건 조회
	@GetMapping("/{contentId}")
	public ResponseEntity<ContentResponse> getContent(@PathVariable Long contentId) {
		return ResponseEntity.ok(contentService.getContent(contentId));
	}

	//어드민 콘텐츠 수정 (PATCH)
	@PatchMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentResponse> patchContent(
		@PathVariable Long contentId,
		@RequestPart("request") ContentPatchRequest.PatchDetail detail,
		@RequestPart(value = "thumbnail", required = false) MultipartFile file) {

		ContentPatchRequest patchRequest = new ContentPatchRequest();
		patchRequest.setRequest(detail);

		return ResponseEntity.ok(contentService.patchContent(contentId, patchRequest, file));
	}

	//어드민 콘텐츠 삭제
	@DeleteMapping("/{contentId}")
	public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
		contentService.deleteContent(contentId);
		return ResponseEntity.ok().build();
	}
}
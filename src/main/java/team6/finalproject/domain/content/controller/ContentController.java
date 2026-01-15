package team6.finalproject.domain.content.controller;

import java.util.List;
import java.util.UUID;

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

	// 1. 콘텐츠 목록 조회 (커서 페이지네이션)
	@GetMapping
	public ResponseEntity<CursorResponse<ContentResponse>> getContents(
		@RequestParam(required = false) String cursor,     // Long -> String
		@RequestParam(required = false) UUID idAfter,       // idAfter 추가
		@RequestParam(defaultValue = "10") int limit,
		@RequestParam(required = false) List<String> tagsIn, // tagsIn 추가
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String sortDirection,
		@RequestParam(required = false) String typeEqual,
		@RequestParam(required = false) String keywordLike) {

		return ResponseEntity.ok(contentService.getContents(
			cursor, idAfter, limit, tagsIn, sortBy, sortDirection, typeEqual, keywordLike
		));
	}
	// 2. 콘텐츠 생성 (어드민)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentResponse> createContent(
		// JSON 데이터를 'request'라는 파트 받는다.
		@RequestPart("request") @Valid ContentCreateRequest request,
		// 파일 데이터는 'thumbnail' 파트로 받되, 없어도(null) 통과되게 한다. (s3연결 전)
		@RequestPart(value = "thumbnail", required = false) org.springframework.web.multipart.MultipartFile file) {

		// 썸네일 파일 처리 로직은 나중에 S3 연동 시 이 부분에 추가
		// 지금은 request DTO에 담긴 정보만 사용하여 저장
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(contentService.createContent(request));
	}

	// 3. 콘텐츠 단건 조회
	@GetMapping("/{contentId}")
	public ResponseEntity<ContentResponse> getContent(@PathVariable Long contentId) {
		return ResponseEntity.ok(contentService.getContent(contentId));
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

		return ResponseEntity.ok(contentService.patchContent(contentId, patchRequest));
	}

	// 5. 어드민 콘텐츠 삭제
	@DeleteMapping("/{contentId}")
	public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
		contentService.deleteContent(contentId);
		return ResponseEntity.ok().build(); //200성공
	}
}
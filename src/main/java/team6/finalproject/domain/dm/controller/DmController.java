package team6.finalproject.domain.dm.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.dm.dto.CursorResponse;
import team6.finalproject.domain.dm.dto.DmCreateRequest;
import team6.finalproject.domain.dm.dto.DmResponse;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.service.DmService;
import team6.finalproject.global.security.MoplUserDetails;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class DmController {

	private final DmService dmService;

	//대화 목록 조회
	@GetMapping
	public ResponseEntity<CursorResponse<DmResponse>> getDmList(
		@AuthenticationPrincipal MoplUserDetails userDetails,
		@RequestParam(required = false) String keywordLike,
		@RequestParam(required = false) String cursor,
		@RequestParam(defaultValue = "10") int limit) {

		List<DmResponse> list = dmService.getDmList(userDetails.getUserDto().id(), cursor, limit, keywordLike);

		// 리스트를 커서 응답으로 감싸서 전달
		return ResponseEntity.ok(new CursorResponse<>(
			list,
			list.isEmpty() ? null : list.get(list.size() - 1).id().toString(),
			null,
			false,
			list.size(),
			"createdAt",
			"DESCENDING"
		));
	}

	@PostMapping
	public ResponseEntity<DmResponse> createDm(
		@AuthenticationPrincipal MoplUserDetails userDetails,
		@RequestBody DmCreateRequest request) {
		return ResponseEntity.ok(dmService.getOrCreateDm(userDetails.getUserDto().id(), request.withUserId()));
	}

	@GetMapping("/{conversationId}")
	public ResponseEntity<DmResponse> getDm(
		@AuthenticationPrincipal MoplUserDetails userDetails,
		@PathVariable Long conversationId) {
		return ResponseEntity.ok(dmService.getDmDetail(conversationId, userDetails.getUserDto().id()));
	}

	//DM 목록 조회 (메시지 내역)
	@GetMapping("/{conversationId}/direct-messages")
	public ResponseEntity<CursorResponse<MessageResponse>> getMessages(
		@PathVariable Long conversationId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(required = false) String idAfter, // 스웨거 명세 반영
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "DESCENDING") String sortDirection, // 필수 파라미터 처리
		@RequestParam(defaultValue = "createdAt") String sortBy // 필수 파라미터 처리
	) {
		return ResponseEntity.ok(dmService.getMessages(conversationId, cursor, limit));
	}

	//특정 사용자와의 대화 조회
	@GetMapping("/with")
	public ResponseEntity<DmResponse> getDmWithUser(
		@AuthenticationPrincipal MoplUserDetails user,
		@RequestParam Long userId) {
		return ResponseEntity.ok(dmService.getOrCreateDm(user.getUserDto().id(), userId));
	}

	//DM 읽음 처리
	@PostMapping("/{dmId}/direct-messages/{messageId}/read")
	public ResponseEntity<Void> readMessage(
		@AuthenticationPrincipal MoplUserDetails user,
		@PathVariable Long dmId,
		@PathVariable Long messageId) {
		dmService.markAsRead(dmId, messageId, user.getUserDto().id());
		return ResponseEntity.ok().build();
	}
}
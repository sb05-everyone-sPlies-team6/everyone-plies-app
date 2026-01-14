package team6.finalproject.domain.dm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team6.finalproject.domain.dm.dto.DirectMessageSendRequest;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.service.DmService;
import team6.finalproject.domain.dm.service.SseService;
import team6.finalproject.global.security.MoplUserDetails;

@Controller
@RequiredArgsConstructor
public class DmMessageController {

	private final SimpMessagingTemplate messagingTemplate;
	private final DmService dmService;
	private final SseService sseService;

	//실시간 메시지 전송
	@MessageMapping("/conversations/{dmId}/direct-messages")
	public void sendMessage(@DestinationVariable Long dmId,
		DirectMessageSendRequest request,
		SimpMessageHeaderAccessor headerAccessor) {

		//보안 컨텍스트에서 발신자 정보 추출
		MoplUserDetails userDetails = (MoplUserDetails) headerAccessor.getUser();
		Long senderId = userDetails.getUserDto().id();

		//메시지 저장 및 DTO 생성
		MessageResponse response = dmService.saveMessage(dmId, senderId, request);

		//웹소켓 구독자들에게 브로드캐스트 (실시간 채팅창 업데이트)
		// SUBSCRIBE /sub/conversations/{dmId}/direct-messages
		messagingTemplate.convertAndSend("/sub/conversations/" + dmId + "/direct-messages", response);

		Long receiverId = response.receiver().userId();
		sseService.sendDmNotification(receiverId, response);
	}
}

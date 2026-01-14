package team6.finalproject.domain.dm.controller;

import java.security.Principal;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;
import team6.finalproject.domain.dm.dto.DirectMessageSendRequest;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.service.DmService;
import team6.finalproject.domain.dm.service.SseService;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DmMessageController {

	private final SimpMessagingTemplate messagingTemplate;
	private final DmService dmService;
	private final SseService sseService;
	private final UserService userService;

	//실시간 메시지 전송
	@MessageMapping("/conversations/{dmId}/direct-messages")
	public void sendMessage(@DestinationVariable Long dmId,
		DirectMessageSendRequest request,
		Principal principal) {

		User sender = userService.getUserByEmail(principal.getName());
		Long senderId = sender.getId();

		MessageResponse response = dmService.saveMessage(dmId, senderId, request);

		messagingTemplate.convertAndSend("/sub/conversations/" + dmId + "/direct-messages", response);

		sseService.sendDmNotification(response.receiver().userId(), response);
	}
}

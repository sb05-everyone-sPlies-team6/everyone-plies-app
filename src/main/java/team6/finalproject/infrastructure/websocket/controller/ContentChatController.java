package team6.finalproject.infrastructure.websocket.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.infrastructure.websocket.dto.ChangeType;
import team6.finalproject.infrastructure.websocket.dto.ContentChatSendRequest;
import team6.finalproject.infrastructure.websocket.dto.WatchingSessionChange;
import team6.finalproject.infrastructure.websocket.service.RealTimeChatService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ContentChatController {

    private final RealTimeChatService chatService;

    // 채팅 전송
    @MessageMapping("/contents/{contentId}/chat")
    public void sendChat(@DestinationVariable String contentId,  // contentId는 String으로 처리
                         ContentChatSendRequest request,
                         Principal principal) {
        String userId = principal.getName();  // userId를 String으로 받아옴
        chatService.sendChat(contentId, userId, request);  // String 타입의 userId 전달
    }

    // 시청자 JOIN
    @MessageMapping("/contents/{contentId}/watch")
    public void joinWatching(@DestinationVariable String contentId, Principal principal) {
        String userId = principal.getName();  // userId를 String으로 받아옴

        // 시청자 리스트에 사용자 추가
        chatService.joinWatching(contentId, userId);

        // JOIN 이벤트 발생 시 실시간 시청자 수와 시청자 목록 전송
        chatService.broadcastWatchingChange(contentId);
    }

    // 시청자 LEAVE
    @MessageMapping("/contents/{contentId}/watch/leave")
    public void leaveWatching(@DestinationVariable String contentId,
                              Principal principal) {
        String userId = principal.getName();  // userId를 String으로 받아옴
        chatService.leaveWatching(contentId, userId);
    }
}
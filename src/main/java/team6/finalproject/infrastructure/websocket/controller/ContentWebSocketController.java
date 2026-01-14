package team6.finalproject.infrastructure.websocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import team6.finalproject.domain.content.dto.*;
import team6.finalproject.domain.user.dto.UserSummary;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.service.UserService;
import team6.finalproject.infrastructure.websocket.dto.*;

import java.security.Principal;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ContentWebSocketController {

    private final UserService userService;

    @MessageMapping("/contents/{contentId}/chat")
    @SendTo("/sub/contents/{contentId}/chat")
    public ContentChatDto sendChat(@DestinationVariable String contentId,
                                   ContentChatSendRequest request,
                                   Principal principal) {

        if (principal == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new IllegalStateException("인증되지 않은 사용자입니다.");
            }
            principal = auth;
        }

        User user = userService.getUserByEmail(principal.getName());
        UserSummary sender = UserSummary.from(user);

        return new ContentChatDto(sender, request.content());
    }
}


package team6.finalproject.infrastructure.websocket.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.service.ContentService;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.service.DmService;
import team6.finalproject.domain.dm.service.SseService;
import team6.finalproject.domain.user.dto.UserSummary;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.service.UserService;
import team6.finalproject.infrastructure.websocket.dto.ChangeType;
import team6.finalproject.infrastructure.websocket.dto.WatchingSessionChange;
import team6.finalproject.infrastructure.websocket.dto.WatchingSessionDto;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ContentService contentService;
    private final DmService dmService;
    private final SseService sseService;

    private final Map<String, Map<String, UserSummary>> watchingUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWatchSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination(); // ex: /sub/contents/1/watch
        String contentId = extractContentId(destination);
        Principal principal = accessor.getUser();
        if (principal == null || contentId == null) return;

        User user = userService.getUserByEmail(principal.getName());
        UserSummary watcher = UserSummary.from(user);

        Map<String, UserSummary> userMap = watchingUsers.computeIfAbsent(contentId, k -> new ConcurrentHashMap<>());

        UserSummary existing = userMap.putIfAbsent(user.getId().toString(), UserSummary.from(user));

        // 중복 Join 방지
        if (existing != null) {
            log.debug("Duplicate JOIN ignored. userId={}, contentId={}", user.getId(), contentId);
            return;
        }

        userMap.put(user.getId().toString(), watcher);

        Content content = contentService.getContentById(Long.parseLong(contentId));
        long count = userMap.size();

        userMap.values().forEach(existingWatcher -> {

            WatchingSessionChange joinChange = new WatchingSessionChange(
                    ChangeType.JOIN,
                    new WatchingSessionDto(
                            existingWatcher.userId(),
                            LocalDateTime.now(),
                            existingWatcher,
                            ContentSummary.from(content)
                    ),
                    count
            );

            messagingTemplate.convertAndSend(
                    "/sub/contents/" + contentId + "/watch",
                    joinChange
            );
        });

        // 기존 유저에게 새 유저 정보 전송
        WatchingSessionChange newUserJoin = new WatchingSessionChange(
                ChangeType.JOIN,
                new WatchingSessionDto(
                        String.valueOf(user.getId()),
                        LocalDateTime.now(),
                        watcher,
                        ContentSummary.from(content)
                ),
                count
        );
        messagingTemplate.convertAndSend("/sub/contents/" + contentId + "/watch", newUserJoin);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Authentication auth = (Authentication) principal;
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername());

        watchingUsers.forEach((contentId, userMap) -> {
            if (userMap.remove(String.valueOf(user.getId())) != null) {
                long count = userMap.size();
                Content content = contentService.getContentById(Long.parseLong(contentId));
                WatchingSessionChange change = new WatchingSessionChange(
                        ChangeType.LEAVE,
                        new WatchingSessionDto(
                                String.valueOf(user.getId()),
                                LocalDateTime.now(),
                                UserSummary.from(user),
                                ContentSummary.from(content)
                        ),
                        count
                );

                messagingTemplate.convertAndSend("/sub/contents/" + contentId + "/watch", change);
            }
        });
    }

    private String extractContentId(String destination) {
        if (destination == null) return null;
        String[] parts = destination.split("/");
        if (parts.length >= 4) return parts[3]; // /sub/contents/{contentId}/watch
        return null;
    }

    @EventListener
    public void handleDmSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith("/sub/conversations/")) return;
        String[] parts = destination.split("/");
        if (parts.length < 5 || !parts[4].equals("direct-messages")) return;

        Long dmId = Long.parseLong(parts[3]);
        Principal principal = accessor.getUser();

        if (principal == null) return;

        User user = userService.getUserByEmail(principal.getName());

        dmService.markAllAsRead(dmId, user.getId());

        var response = dmService.getMessages(dmId, null, 1);
        if (!response.data().isEmpty()) {
            MessageResponse latest = response.data().get(0);

            sseService.sendDmNotification(user.getId(), latest);
        }
    }
}

package team6.finalproject.infrastructure.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.service.ContentService;
import team6.finalproject.domain.user.dto.UserSummary;
import team6.finalproject.domain.user.service.UserService;
import team6.finalproject.infrastructure.websocket.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    // contentId → 현재 시청자(userId) 집합
    private final Map<String, Set<String>> contentWatchers = new ConcurrentHashMap<>();  // contentId, userId를 모두 String으로 처리

    // 채팅 전송
    public void sendChat(String contentId, String userId, ContentChatSendRequest request) {
        UserSummary sender = userService.getUserSummary(userId);  // String 타입의 userId를 사용
        ContentChatDto chat = new ContentChatDto(sender, request.content());

        // WebSocket을 통해 채팅 메시지 전송
        messagingTemplate.convertAndSend(
                "/sub/contents/" + contentId + "/chat",  // 구독된 채팅 경로로 메시지 전송
                chat  // 채팅 객체
        );
    }

    // 시청자 JOIN
    public void joinWatching(String contentId, String userId) {
        contentWatchers
                .computeIfAbsent(contentId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);  // String userId 처리

        broadcastWatchingChange(contentId);  // JOIN 후 시청자 목록 갱신
    }

    // 시청자 LEAVE
    public void leaveWatching(String contentId, String userId) {
        Set<String> watchers = contentWatchers.get(contentId);
        if (watchers != null && watchers.remove(userId)) {
            broadcastWatchingChange(contentId);  // LEAVE 후 시청자 목록 갱신
        }
    }

    public void broadcastWatchingChange(String contentId) {
        Set<String> watchers = contentWatchers.getOrDefault(contentId, Set.of());
        int watcherCount = watchers.size();  // 현재 시청자 수

        log.debug("Updated watcher count for content {}: {}", contentId, watcherCount);

        // WatchingSessionDto 생성
        WatchingSessionDto session = watchers.stream()
                .map(id -> {
                    UserSummary user = userService.getUserSummary(id);  // String userId 사용
                    if (user == null) {
                        user = new UserSummary("0", "unknown", "");  // 기본값 처리
                    }
                    ContentSummary content = ContentSummary.fromId(Long.valueOf(contentId));  // contentId는 String
                    String sessionId = (contentId != null) ? contentId : "-1";  // String으로 처리
                    return new WatchingSessionDto(sessionId, LocalDateTime.now(), user, content);
                })
                .findFirst()
                .orElse(null);

        // WatchingSessionChange 객체 생성
        WatchingSessionChange change = new WatchingSessionChange(
                ChangeType.JOIN,  // JOIN 또는 LEAVE에 따라 처리
                session,
                watcherCount  // 시청자 수 갱신
        );

        // WebSocket을 통해 실시간 시청자 수와 목록을 클라이언트로 전송
        messagingTemplate.convertAndSend(
                "/sub/contents/" + contentId + "/watch",  // WebSocket 경로
                change
        );
    }
}


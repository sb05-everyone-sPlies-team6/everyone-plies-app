package team6.finalproject.infrastructure.websocket.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.service.ContentService;
import team6.finalproject.domain.user.dto.UserSummary;
import team6.finalproject.infrastructure.websocket.dto.ChangeType;
import team6.finalproject.infrastructure.websocket.dto.WatchingSessionChange;
import team6.finalproject.infrastructure.websocket.dto.WatchingSessionDto;
import team6.finalproject.infrastructure.websocket.manager.PresenceManager;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WatchingCleanupScheduler {

    private final PresenceManager presenceManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final ContentService contentService;

    private final Map<String, Map<String, UserSummary>> watchingUsers = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 10_000) // 10초마다 실행
    public void cleanupOfflineUsers() {
        watchingUsers.forEach((contentId, userMap) -> {
            Iterator<Map.Entry<String, UserSummary>> iter = userMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, UserSummary> entry = iter.next();
                String userId = entry.getKey();
                UserSummary summary = entry.getValue();

                if (!presenceManager.isOnline(userId)) {
                    iter.remove(); // Map에서 제거

                    WatchingSessionChange leave = new WatchingSessionChange(
                            ChangeType.LEAVE,
                            new WatchingSessionDto(
                                    userId,
                                    LocalDateTime.now(),
                                    summary,
                                    ContentSummary.from(contentService.getContentById(Long.parseLong(contentId)))
                            ),
                            userMap.size()
                    );

                    messagingTemplate.convertAndSend(
                            "/sub/contents/" + contentId + "/watch",
                            leave
                    );
                }
            }
        });
    }
}
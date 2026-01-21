package team6.finalproject.infrastructure.websocket.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceManager {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl = Duration.ofSeconds(30); // TTL 30초

    public void online(String userId) {
        redisTemplate.opsForValue().set(userId, "online", ttl);
        log.debug("User {} set online with TTL {}s", userId, ttl.getSeconds());
    }

    public boolean isOnline(String userId) {
        boolean online = redisTemplate.hasKey(userId);
        log.debug("User {} online? {}", userId, online);
        return online;
    }

    public void offline(String userId) {
        redisTemplate.delete(userId);
    }
}

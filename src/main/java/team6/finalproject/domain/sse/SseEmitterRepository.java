package team6.finalproject.domain.sse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// SSE 연결, 관리

@Repository
@Slf4j
public class SseEmitterRepository {

  private final ConcurrentMap<Long, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public SseEmitter save(Long receiverId, SseEmitter emitter) {
    log.info("[SSE] register emitter userId={}", receiverId);
    data.compute(receiverId, (key, emitters) -> {
      if (emitters == null) {
        return new CopyOnWriteArrayList<>(List.of(emitter));
      } else {
        emitters.add(emitter);
        return emitters;
      }
    });
    return emitter;
  }

  public void delete(Long receiverId, SseEmitter emitter) {
    data.computeIfPresent(receiverId, (key, emiiters) -> {
      emiiters.remove(emitter);
      return emiiters.isEmpty() ? null : emiiters;
    });
  }

}

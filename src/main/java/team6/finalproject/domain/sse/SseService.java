package team6.finalproject.domain.sse;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.repository.NotificationRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

  private static final long TIMEOUT = 60L * 60 * 1000;
  private final SseEmitterRepository sseEmitterRepository;
  private final NotificationRepository notificationRepository;

  // 연결
  public SseEmitter connect(Long receiverId, Long lastId) {
    log.info("[SSE] connect userId={}", receiverId);

    SseEmitter emitter = new SseEmitter(TIMEOUT);

    emitter.onCompletion(() -> {
      sseEmitterRepository.delete(receiverId, emitter);
    });
    emitter.onTimeout(() -> {
      sseEmitterRepository.delete(receiverId, emitter);
    });
    emitter.onError((e) -> {
      sseEmitterRepository.delete(receiverId, emitter);
    });

    sseEmitterRepository.save(receiverId, emitter);

    try {
      emitter.send(
          SseEmitter.event()
              .name("connected")
              .data("ok")
      );

      if (lastId != null) {
        List<Notification> missed = notificationRepository.findTop100ByUserIdAndIdGreaterThanOrderByIdAsc(
            receiverId, lastId);

        for (Notification n : missed) {
          NotificationDto dto = new NotificationDto(
              n.getId(),
              n.getCreatedAt(),
              n.getUser().getId(),
              n.getTitle(),
              n.getContent(),
              n.getLevel()
          );

          emitter.send(
              SseEmitter.event()
                  .id(n.getId().toString()).name("notifications").data(dto)
          );
        }
      }

    } catch (Exception e) {
      emitter.completeWithError(e);
      sseEmitterRepository.delete(receiverId, emitter);
    }

    return emitter;
  }

  public void send(Collection<Long> receiverIds, String eventName, NotificationDto dto) {
    List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverIdsIn(receiverIds);

    emitters.forEach(emitter -> {
      try {
        emitter.send(
            SseEmitter.event()
                .id(dto.id().toString())
                .name("notifications")
                .data(dto)
        );
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
    });
  }
}

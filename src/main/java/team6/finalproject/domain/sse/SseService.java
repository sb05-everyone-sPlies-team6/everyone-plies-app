package team6.finalproject.domain.sse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.repository.NotificationRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

  private static final long TIMEOUT = 60L * 60 * 1000;
  private static final String HEARTBEAT_EVENT = "heartbeat";

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

  public void sendReadNotification(Long userId, Long dmId) {
    List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverIdsIn(List.of(userId));

    emitters.forEach(emitter -> {
      try {
        emitter.send(
            SseEmitter.event()
                .name("direct-messages-read")
                .data(Map.of("conversationId", dmId))
        );
      } catch (Exception e) {
        log.error("Failed to send read notification to user {}", userId, e);
        emitter.completeWithError(e);
        sseEmitterRepository.delete(userId, emitter);
      }
    });
  }
  public void sendDmNotification(Long receiverId, MessageResponse messageResponse) {
    List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverIdsIn(List.of(receiverId));

    emitters.forEach(emitter -> {
      try {
        emitter.send(
            SseEmitter.event()
                .name("direct-messages")
                .data(messageResponse)
        );
      } catch (Exception e) {
        log.error("Failed to send DM notification to user {}", receiverId, e);
        emitter.completeWithError(e);
        sseEmitterRepository.delete(receiverId, emitter);
      }
    });
  }

  @Scheduled(fixedRate = 25_000)
  public void heartbeat() {
    log.info("[SSE] heartbeat");

    List<SseEmitter> emitters = sseEmitterRepository.findAll();

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .name(HEARTBEAT_EVENT)
            .data("ping"));
      } catch (Exception e) {
        emitter.completeWithError(e);
        sseEmitterRepository.delete(emitter);
      }
    }
  }
}

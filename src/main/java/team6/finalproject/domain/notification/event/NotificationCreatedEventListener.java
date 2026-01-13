package team6.finalproject.domain.notification.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team6.finalproject.domain.sse.SseService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCreatedEventListener {

  private final SseService sseService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationCreatedEvent event) {
    log.info("[Event] receiver={} messageId={}", event.dto().receiverId(), event.dto().id());

    sseService.send(
        List.of(event.dto().id()),
        "notifications",
        event.dto()
    );
  }
}

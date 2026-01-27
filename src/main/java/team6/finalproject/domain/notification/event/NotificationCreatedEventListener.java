package team6.finalproject.domain.notification.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

//  @Value("${notification.test.delay-ms:0}")
//  private long delayMs;

  @Async("notiExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationCreatedEvent event) {
//    long t0 = System.nanoTime();
//
//    if (delayMs > 0) {
//      try {
//        Thread.sleep(delayMs);
//      } catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//      }
//    }

    sseService.send(
        List.of(event.dto().receiverId()),
        "notifications",
        event.dto()
    );

//    long notiMs = (System.nanoTime() - t0) / 1_000_000;
//    log.info("[NOTI][SYNC] receiver={} messageId={} NOTI={}ms (delay={}ms)",
//        event.dto().receiverId(), event.dto().id(), notiMs, delayMs
//    );
  }
}

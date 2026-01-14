package team6.finalproject.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.domain.notification.dto.CursorResponse;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorResponse<NotificationDto>> findAll(String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {
    CursorResponse<NotificationDto> all = notificationService.findAll(cursor, idAfter, limit,
        sortDirection, sortBy);

    return ResponseEntity.status(HttpStatus.OK).body(all);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(@PathVariable Long notificationId) {
    notificationService.delete(notificationId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}

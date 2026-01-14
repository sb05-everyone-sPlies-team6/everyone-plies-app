package team6.finalproject.domain.notification.dto;

import java.time.LocalDateTime;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;

public record NotificationDto(
    Long id,
    LocalDateTime createdAt,
    Long receiverId,
    String title,
    String content,
    Level level
) {

  public static NotificationDto from(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getUser().getId(),
        notification.getTitle(),
        notification.getContent(),
        notification.getLevel()
    );
  }

}

package team6.finalproject.domain.notification.dto;

import java.time.LocalDateTime;
import team6.finalproject.domain.notification.entity.Level;

public record NotificationDto(
    Long id,
    LocalDateTime createdAt,
    Long receiverId,
    String title,
    String content,
    Level level
) {

}

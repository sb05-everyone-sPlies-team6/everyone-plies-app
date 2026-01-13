package team6.finalproject.domain.notification.event;

import team6.finalproject.domain.notification.dto.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto dto
) {

}

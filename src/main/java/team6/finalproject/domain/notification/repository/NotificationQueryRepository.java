package team6.finalproject.domain.notification.repository;

import team6.finalproject.domain.notification.dto.CursorResponse;
import team6.finalproject.domain.notification.dto.NotificationDto;

public interface NotificationQueryRepository {
  CursorResponse<NotificationDto> findAll(Long userId, String cursor, Long idAfter, int limit, String sortDirection, String sortBy);

}

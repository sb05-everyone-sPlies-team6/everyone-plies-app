package team6.finalproject.domain.notification.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.notification.dto.CursorResponse;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public CursorResponse<NotificationDto> findAll(String cursor, Long idAfter, int limit, String sortDirection, String sortBy) {
    return notificationRepository.findAll(cursor, idAfter, limit, sortDirection, sortBy);
  }

  public void delete(Long id) {
    Notification notification = notificationRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Notification with id " + id + " does not exist"));
    notificationRepository.delete(notification);
  }

}

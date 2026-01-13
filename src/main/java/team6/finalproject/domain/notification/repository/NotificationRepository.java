package team6.finalproject.domain.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team6.finalproject.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findTop100ByUserIdAndIdGreaterThanOrderByIdAsc(Long receiverId, Long lastId);

}

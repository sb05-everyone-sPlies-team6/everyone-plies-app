package team6.finalproject.domain.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team6.finalproject.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationQueryRepository {
  List<Notification> findTop100ByUserIdAndIdGreaterThanOrderByIdAsc(Long receiverId, Long lastId);

}

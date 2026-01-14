package team6.finalproject.domain.dm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import team6.finalproject.domain.dm.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long>, MessageRepositoryCustom {
	@Modifying
	@Query("UPDATE Message m SET m.isRead = true WHERE m.id = :messageId AND m.dmId = :dmId")
	void markAsRead(@Param("dmId") Long dmId, @Param("messageId") Long messageId);

	@Modifying
	@Query("UPDATE Message m SET m.isRead = true WHERE m.dmId = :dmId AND m.userId != :currentUserId")
	void markAllAsRead(@Param("dmId") Long dmId, @Param("currentUserId") Long currentUserId);
}
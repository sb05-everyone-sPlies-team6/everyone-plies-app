package team6.finalproject.domain.dm.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dm_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	@Column(name = "conversation_id") // DB 컬럼명 유지
	private Long dmId;

	@Column(name = "user_id")
	private Long userId;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_read")
	private boolean isRead = false;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	// 읽음 처리 메서드
	public void read() {
		this.isRead = true;
	}
}
package team6.finalproject.domain.dm.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DmParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "participant_id")
	private Long id;

	@Column(name = "conversation_id", nullable = false)
	private Long dmId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "participant_chat")
	private String participantChat;

	@Column(name = "leave_chat")
	private String leaveChat;

	@CreatedDate
	@Column(name = "joined_at")
	private LocalDateTime joinedAt;

	// 편리한 생성을 위한 생성자
	public DmParticipant(Long dmId, Long userId) {
		this.dmId = dmId;
		this.userId = userId;
	}
}
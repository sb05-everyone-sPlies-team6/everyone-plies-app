package team6.finalproject.domain.dm.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Dm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "conversation_id")
	private Long id;

	@Column(name = "name", length = 50)
	private String name;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static Dm create() {
		Dm dm = new Dm();
		dm.createdAt = LocalDateTime.now(); // 수동으로 현재 시간 세팅
		return dm;
	}
}

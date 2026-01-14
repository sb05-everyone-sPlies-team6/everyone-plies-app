package team6.finalproject.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.finalproject.domain.user.entity.User;

@Entity
@Table(name = "notifications")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long id;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Level level;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "target_id")
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private TargetType targetType;

  public Notification(User user, String title, String content, Level level) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.level = level;
    this.createdAt = LocalDateTime.now();
  }
}

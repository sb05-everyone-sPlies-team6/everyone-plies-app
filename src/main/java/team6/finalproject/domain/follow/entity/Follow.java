package team6.finalproject.domain.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "follow_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User followee;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public Follow(User follower, User followee) {
    this.follower = follower;
    this.followee = followee;
    this.createdAt = LocalDateTime.now();
  }
}

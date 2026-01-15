package team6.finalproject.domain.playlist.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlists_subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "playlist_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaylistSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PlaylistSubscription(Long userId, Long playlistId) {
        this.userId = userId;
        this.playlistId = playlistId;
        this.createdAt = LocalDateTime.now();
    }
}
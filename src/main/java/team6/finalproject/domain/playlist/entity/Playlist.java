package team6.finalproject.domain.playlist.entity;

import jakarta.persistence.*;
import lombok.*;
import team6.finalproject.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User owner;

    private String title;

    private String description;

    @Column(name = "total_subscription")
    private int totalSubscription;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Playlist create(Long userId, String title, String description) {
        Playlist p = new Playlist();
        p.userId = userId;
        p.title = title;
        p.description = description;
        p.totalSubscription = 0;
        p.createdAt = LocalDateTime.now();
        p.updatedAt = LocalDateTime.now();
        return p;
    }

}

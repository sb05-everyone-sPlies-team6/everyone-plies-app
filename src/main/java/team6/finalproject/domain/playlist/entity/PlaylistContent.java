package team6.finalproject.domain.playlist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlists_contents")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PlaylistContent(Long playlistId, Long contentId) {
        this.playlistId = playlistId;
        this.contentId = contentId;
    }

}

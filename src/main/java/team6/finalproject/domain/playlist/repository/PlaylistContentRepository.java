package team6.finalproject.domain.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.finalproject.domain.playlist.entity.PlaylistContent;

import java.util.List;
import java.util.Optional;

public interface PlaylistContentRepository extends JpaRepository<PlaylistContent, Long> {
    Optional<PlaylistContent> findByPlaylistIdAndContentId(Long playlistId, Long contentId);
    List<PlaylistContent> findAllByPlaylistId(Long playlistId);
}

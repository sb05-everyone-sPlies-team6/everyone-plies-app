package team6.finalproject.domain.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.finalproject.domain.playlist.entity.PlaylistSubscription;

import java.util.Optional;

public interface PlaylistSubscriptionRepository extends JpaRepository<PlaylistSubscription, Long> {
    Optional<PlaylistSubscription> findByUserIdAndPlaylistId(Long userId, Long playlistId);
}

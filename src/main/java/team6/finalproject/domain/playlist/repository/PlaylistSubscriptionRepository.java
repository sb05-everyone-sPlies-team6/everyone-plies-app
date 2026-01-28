package team6.finalproject.domain.playlist.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team6.finalproject.domain.playlist.entity.PlaylistSubscription;

import java.util.Optional;
import team6.finalproject.domain.user.entity.User;

public interface PlaylistSubscriptionRepository extends JpaRepository<PlaylistSubscription, Long> {
    Optional<PlaylistSubscription> findByUserIdAndPlaylistId(Long userId, Long playlistId);
    List<Long> findUserIdsByPlaylistId(Long playlistId);
}

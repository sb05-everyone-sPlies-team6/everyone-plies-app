package team6.finalproject.domain.playlist.repository;

import team6.finalproject.domain.playlist.entity.Playlist;

import java.util.List;

public interface PlaylistQueryRepository {
    List<Playlist> findPlaylists(
            Long ownerId,
            Long subscriberId,
            String keyword,
            int limit,
            String sortBy,
            String sortDirection,
            String cursor,
            String idAfter
    );

    long countPlaylists(Long ownerId, Long subscriberId, String keyword);
}


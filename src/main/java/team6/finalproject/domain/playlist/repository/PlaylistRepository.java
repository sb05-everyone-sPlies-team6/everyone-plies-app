package team6.finalproject.domain.playlist.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team6.finalproject.domain.playlist.entity.Playlist;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, PlaylistQueryRepository {

    @Query("""
            SELECT p FROM Playlist p
            WHERE (:cursor IS NULL OR p.updatedAt < :cursor)
            ORDER BY
            CASE WHEN :sortBy = 'updatedAt' AND :sortDirection = 'DESCENDING' THEN p.updatedAt END DESC,
            CASE WHEN :sortBy = 'updatedAt' AND :sortDirection = 'ASCENDING' THEN p.updatedAt END ASC
            """)
    List<Playlist> findAllSorted(int limit, String sortBy, String sortDirection, String cursor);
}

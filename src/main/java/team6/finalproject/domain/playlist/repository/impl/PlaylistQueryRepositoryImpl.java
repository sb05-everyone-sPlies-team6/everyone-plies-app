package team6.finalproject.domain.playlist.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.playlist.entity.Playlist;
import team6.finalproject.domain.playlist.entity.QPlaylist;
import team6.finalproject.domain.playlist.entity.QPlaylistSubscription;
import team6.finalproject.domain.playlist.repository.PlaylistQueryRepository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PlaylistQueryRepositoryImpl implements PlaylistQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Playlist> findPlaylists(
            Long ownerId,
            Long subscriberId,
            String keyword,
            int limit,
            String sortBy,
            String sortDirection,
            String cursor,
            String idAfter
    ) {
        QPlaylist p = QPlaylist.playlist;
        QPlaylistSubscription ps = QPlaylistSubscription.playlistSubscription;

        BooleanBuilder where = new BooleanBuilder();

        if (ownerId != null) {
            where.and(p.userId.eq(ownerId));
        }

        if (keyword != null) {
            where.and(p.title.containsIgnoreCase(keyword));
        }

        if (subscriberId != null) {
            where.and(
                    p.id.in(
                            queryFactory
                                    .select(ps.playlistId)
                                    .from(ps)
                                    .where(ps.userId.eq(subscriberId))
                    )
            );

            // 🔥 핵심 수정: 내 플레이리스트 제외
            where.and(p.userId.ne(subscriberId));
        }

        // 커서
        if (cursor != null) {
            where.and(p.updatedAt.lt(LocalDateTime.parse(cursor)));
        }

        OrderSpecifier<?> order = sortBy.equals("subscribeCount")
                ? (sortDirection.equals("ASCENDING")
                ? p.totalSubscription.asc()
                : p.totalSubscription.desc())
                : (sortDirection.equals("ASCENDING")
                ? p.updatedAt.asc()
                : p.updatedAt.desc());

        return queryFactory
                .selectFrom(p)
                .where(where)
                .orderBy(order, p.id.desc()) // tie-breaker
                .limit(limit + 1)
                .fetch();
    }

    @Override
    public long countPlaylists(Long ownerId, Long subscriberId, String keyword) {
        QPlaylist p = QPlaylist.playlist;
        QPlaylistSubscription ps = QPlaylistSubscription.playlistSubscription;

        BooleanBuilder where = new BooleanBuilder();

        if (ownerId != null) {
            where.and(p.userId.eq(ownerId));
        }

        if (keyword != null) {
            where.and(p.title.containsIgnoreCase(keyword));
        }

        if (subscriberId != null) {
            where.and(
                    p.id.in(
                            queryFactory
                                    .select(ps.playlistId)
                                    .from(ps)
                                    .where(ps.userId.eq(subscriberId))
                    )
            );

            // 🔥 여기에도 동일하게 적용
            where.and(p.userId.ne(subscriberId));
        }

        return queryFactory
                .select(p.count())
                .from(p)
                .where(where)
                .fetchOne();
    }
}


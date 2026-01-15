package team6.finalproject.domain.playlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.playlist.dto.CursorResponsePlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistCreateRequest;
import team6.finalproject.domain.playlist.entity.Playlist;
import team6.finalproject.domain.playlist.entity.PlaylistContent;
import team6.finalproject.domain.playlist.entity.PlaylistSubscription;
import team6.finalproject.domain.playlist.repository.PlaylistContentRepository;
import team6.finalproject.domain.playlist.repository.PlaylistRepository;
import team6.finalproject.domain.playlist.repository.PlaylistSubscriptionRepository;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.domain.user.dto.UserSummary;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistContentRepository playlistContentRepository;
    private final ContentRepository contentRepository;
    private final PlaylistSubscriptionRepository playlistSubscriptionRepository;

    @Transactional
    public PlaylistDto createPlaylist(Long userId, PlaylistCreateRequest request) {
        Playlist playlist = Playlist.create(userId, request.title(), request.description());
        playlistRepository.save(playlist);

        var owner = userRepository.findById(userId)
                .map(u -> new UserSummary(
                        String.valueOf(u.getId()),
                        u.getName(),
                        u.getProfileImageUrl()
                ))
                .orElse(new UserSummary("unknown", "unknown", null));

        return new PlaylistDto(
                String.valueOf(playlist.getId()),
                owner,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getTotalSubscription(),
                false,
                0,
                List.of()
        );
    }

    public CursorResponsePlaylistDto<PlaylistDto> getPlaylists(
            Long userId,
            int limit,
            String sortBy,
            String sortDirection,
            String cursor
    ) {
        List<Playlist> playlists = playlistRepository.findAllSorted(limit, sortBy, sortDirection, cursor);

        List<PlaylistDto> data = playlists.stream().map(p -> {
            var owner = userRepository.findById(p.getUserId())
                    .map(u -> new UserSummary(
                            String.valueOf(u.getId()),
                            u.getName(),
                            u.getProfileImageUrl()
                    ))
                    .orElse(new UserSummary("unknown", "unknown", null));

            List<ContentSummary> contents = playlistContentRepository.findAllByPlaylistId(p.getId())
                    .stream()
                    .map(pc -> contentRepository.findById(pc.getContentId())
                            .map(ContentSummary::from)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            boolean subscribedByMe = userId != null &&
                    playlistSubscriptionRepository.findByUserIdAndPlaylistId(userId, p.getId()).isPresent();

            return new PlaylistDto(
                    String.valueOf(p.getId()),
                    owner,
                    p.getTitle(),
                    p.getDescription(),
                    p.getUpdatedAt(),
                    p.getTotalSubscription(),
                    subscribedByMe,
                    contents.size(),
                    List.copyOf(contents)
            );
        }).toList();

        boolean hasNext = data.size() > limit;
        Playlist last = data.isEmpty() ? null : playlists.get(Math.min(data.size(), limit) - 1);
        String nextCursor = last == null ? null : last.getUpdatedAt().toString();

        return new CursorResponsePlaylistDto<>(
                data.stream().limit(limit).collect(Collectors.toList()),
                nextCursor,
                last == null ? null : String.valueOf(last.getId()),
                hasNext,
                playlistRepository.count(),
                sortBy,
                sortDirection
        );
    }

    public PlaylistDto getPlaylistById(Long playlistId, Long userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        var owner = userRepository.findById(playlist.getUserId())
                .map(u -> new UserSummary(
                        String.valueOf(u.getId()),
                        u.getName(),
                        u.getProfileImageUrl()
                ))
                .orElse(new UserSummary("unknown", "unknown", null));

        List<ContentSummary> contents = playlistContentRepository.findAllByPlaylistId(playlistId)
                .stream()
                .map(pc -> contentRepository.findById(pc.getContentId())
                        .map(ContentSummary::from)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        boolean subscribedByMe = userId != null &&
                playlistSubscriptionRepository.findByUserIdAndPlaylistId(userId, playlistId).isPresent();

        return new PlaylistDto(
                String.valueOf(playlist.getId()),
                owner,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getTotalSubscription(),
                subscribedByMe,
                contents.size(),
                List.copyOf(contents)
        );
    }

    @Transactional
    public void addContentToPlaylist(Long playlistId, Long contentId) {
        if (playlistContentRepository.findByPlaylistIdAndContentId(playlistId, contentId).isPresent()) {
            return;
        }

        PlaylistContent playlistContent = new PlaylistContent(playlistId, contentId);
        playlistContentRepository.save(playlistContent);
    }

    @Transactional
    public void removeContentFromPlaylist(Long playlistId, Long contentId) {
        playlistContentRepository.findByPlaylistIdAndContentId(playlistId, contentId)
                .ifPresent(playlistContentRepository::delete);
    }

    @Transactional
    public void subscribePlaylist(Long playlistId, Long userId) {
        boolean exists = playlistSubscriptionRepository
                .findByUserIdAndPlaylistId(userId, playlistId)
                .isPresent();
        if (exists) return;

        playlistSubscriptionRepository.save(new PlaylistSubscription(userId, playlistId));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));
        playlist.setTotalSubscription(playlist.getTotalSubscription() + 1);
    }

    @Transactional
    public void unsubscribePlaylist(Long playlistId, Long userId) {
        playlistSubscriptionRepository.findByUserIdAndPlaylistId(userId, playlistId)
                .ifPresent(ps -> {
                    playlistSubscriptionRepository.delete(ps);

                    Playlist playlist = playlistRepository.findById(playlistId)
                            .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));
                    playlist.setTotalSubscription(Math.max(0, playlist.getTotalSubscription() - 1));
                });
    }
}

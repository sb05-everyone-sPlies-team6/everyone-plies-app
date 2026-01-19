package team6.finalproject.domain.playlist.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.TargetType;
import team6.finalproject.domain.notification.repository.NotificationRepository;
import team6.finalproject.domain.playlist.dto.CursorResponsePlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistCreateRequest;
import team6.finalproject.domain.playlist.dto.PlaylistUpdateRequest;
import team6.finalproject.domain.playlist.entity.Playlist;
import team6.finalproject.domain.playlist.entity.PlaylistContent;
import team6.finalproject.domain.playlist.entity.PlaylistSubscription;
import team6.finalproject.domain.playlist.repository.PlaylistContentRepository;
import team6.finalproject.domain.playlist.repository.PlaylistRepository;
import team6.finalproject.domain.playlist.repository.PlaylistSubscriptionRepository;
import team6.finalproject.domain.user.entity.User;
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
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

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
    public void deletePlaylist(Long playlistId, Long currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        // 작성자 체크
        if (!playlist.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("플레이리스트 작성자만 삭제할 수 있습니다.");
        }

        playlistRepository.delete(playlist); // JPA에서 delete
    }

    @Transactional
    public void removeContentFromPlaylist(Long playlistId, Long contentId, Long currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        if (!playlist.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("플레이리스트 작성자만 콘텐츠를 삭제할 수 있습니다.");
        }

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

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Notification notification = new Notification(
            playlist.getOwner(),
            "PLAYLIST SUBSCRIBE",
            user.getName() + "님이 회원님의 플레이리스트를 구독했습니다.",
            Level.INFO,
            playlist.getOwner().getId(),
            TargetType.PLAYLIST_SUBSCRIBED
        );

        notificationRepository.save(notification);
        NotificationDto saved = NotificationDto.from(notification);
        eventPublisher.publishEvent(saved);
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


    @Transactional
    public void updatePlaylist(Long playlistId, Long userId, PlaylistUpdateRequest request) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        if (!playlist.getOwner().getId().equals(userId)) {
            throw new SecurityException("플레이리스트 작성자만 수정할 수 있습니다.");
        }

        if (request.title() != null) playlist.setTitle(request.title());
        if (request.description() != null) playlist.setDescription(request.description());
    }
}

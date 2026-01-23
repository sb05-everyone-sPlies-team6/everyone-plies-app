package team6.finalproject.domain.playlist.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.content.dto.ContentSummary;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.follow.repository.FollowRepository;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.TargetType;
import team6.finalproject.domain.notification.event.NotificationCreatedEvent;
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
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PlaylistDto createPlaylist(Long userId, PlaylistCreateRequest request) {

        User ownerUser = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        Playlist playlist = Playlist.create(userId, request.title(), request.description());
        playlistRepository.save(playlist);

//        var owner = userRepository.findById(userId)
//                .map(u -> new UserSummary(
//                        String.valueOf(u.getId()),
//                        u.getName(),
//                        u.getProfileImageUrl()
//                ))
//                .orElse(new UserSummary("unknown", "unknown", null));

        UserSummary owner = new UserSummary(
            String.valueOf(ownerUser.getId()),
            ownerUser.getName(),
            ownerUser.getProfileImageUrl()
        );

        List<User> followers = followRepository.findFollowersByFolloweeId(userId);
        List<Notification> notifications = followers.stream()
            .map(receiver -> new Notification(
                receiver,
                "PLAYLIST_CREATED",
                owner.name() + "님이 새 플레이리스트를 만들었습니다: " + playlist.getTitle(),
                Level.INFO,
                ownerUser.getId(),
                TargetType.FOLLOWING_USER_ACTIVITY
            ))
            .toList();

        notificationRepository.saveAll(notifications);

        notifications.stream()
            .map(NotificationDto::from)
            .forEach(dto -> eventPublisher.publishEvent(new NotificationCreatedEvent(dto)));

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
            Long viewerId,
            String ownerIdEqual,
            String subscriberIdEqual,
            String keywordLike,
            int limit,
            String sortBy,
            String sortDirection,
            String cursor,
            String idAfter
    ) {
        List<Playlist> playlists = playlistRepository.findPlaylists(
                ownerIdEqual != null ? Long.valueOf(ownerIdEqual) : null,
                subscriberIdEqual != null ? Long.valueOf(subscriberIdEqual) : null,
                keywordLike,
                limit,
                sortBy,
                sortDirection,
                cursor,
                idAfter
        );

        boolean hasNext = playlists.size() > limit;
        List<Playlist> sliced = playlists.stream().limit(limit).toList();

        List<PlaylistDto> data = sliced.stream()
                .map(p -> {
                    UserSummary owner = userRepository.findById(p.getUserId())
                            .map(UserSummary::from)
                            .orElse(UserSummary.unknown());

                    List<ContentSummary> contents =
                            playlistContentRepository.findAllByPlaylistId(p.getId())
                                    .stream()
                                    .map(pc -> contentRepository.findById(pc.getContentId())
                                            .map(ContentSummary::from)
                                            .orElse(null))
                                    .filter(Objects::nonNull)
                                    .toList();

                    boolean subscribedByMe = false;

                    if (viewerId != null && !p.getUserId().equals(viewerId)) {
                        subscribedByMe =
                                playlistSubscriptionRepository
                                        .findByUserIdAndPlaylistId(viewerId, p.getId())
                                        .isPresent();
                    }

                    return PlaylistDto.from(p, owner, contents, subscribedByMe);
                })
                .toList();

        Playlist last = sliced.isEmpty() ? null : sliced.get(sliced.size() - 1);

        return new CursorResponsePlaylistDto<>(
                data,
                last == null ? null : last.getUpdatedAt().toString(),
                last == null ? null : String.valueOf(last.getId()),
                hasNext,
                playlistRepository.countPlaylists(
                        ownerIdEqual != null ? Long.valueOf(ownerIdEqual) : null,
                        subscriberIdEqual != null ? Long.valueOf(subscriberIdEqual) : null,
                        keywordLike
                ),
                sortBy,
                sortDirection
        );
    }

    public PlaylistDto getPlaylistById(Long playlistId, Long viewerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        UserSummary owner = userRepository.findById(playlist.getUserId())
                .map(UserSummary::from)
                .orElse(UserSummary.unknown());

        List<ContentSummary> contents =
                playlistContentRepository.findAllByPlaylistId(playlistId)
                        .stream()
                        .map(pc -> contentRepository.findById(pc.getContentId())
                                .map(ContentSummary::from)
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

        boolean subscribedByMe = false;

        if (viewerId != null && !playlist.getUserId().equals(viewerId)) {
            subscribedByMe =
                    playlistSubscriptionRepository
                            .findByUserIdAndPlaylistId(viewerId, playlistId)
                            .isPresent();
        }

        return PlaylistDto.from(playlist, owner, contents, subscribedByMe);
    }

    @Transactional
    public void addContentToPlaylist(Long playlistId, Long contentId) {
        if (playlistContentRepository.findByPlaylistIdAndContentId(playlistId, contentId).isPresent()) {
            return;
        }

        PlaylistContent playlistContent = new PlaylistContent(playlistId, contentId);
        playlistContentRepository.save(playlistContent);

        Playlist playList = playlistRepository.findById(playlistId).orElseThrow(
            () -> new NoSuchElementException("플레이리스트가 존재하지 않습니다")
        );

        List<User> followers = followRepository.findFollowersByFolloweeId(
            playList.getOwner().getId());

        List<Notification> notifications = followers.stream()
            .filter(follower -> !follower.getId().equals(playList.getOwner().getId()))
            .map(receiver -> new Notification(
                receiver,
                "PLAYLIST_ADDED",
                playList.getTitle() + "에 새 콘텐츠가 추가되었습니다.",
                Level.INFO,
                playList.getOwner().getId(),
                TargetType.PLAYLIST_CONTENT_ADDED
            ))
            .toList();

        notificationRepository.saveAll(notifications);

        notifications.stream()
            .map(NotificationDto::from)
            .forEach(dto -> eventPublisher.publishEvent(new NotificationCreatedEvent(dto)));
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
            playlistId,
            TargetType.PLAYLIST_SUBSCRIBED
        );

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = NotificationDto.from(saved);
        eventPublisher.publishEvent(dto);
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

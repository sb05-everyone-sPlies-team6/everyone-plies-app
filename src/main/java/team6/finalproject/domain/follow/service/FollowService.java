package team6.finalproject.domain.follow.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.follow.dto.FollowDto;
import team6.finalproject.domain.follow.dto.FollowRequest;
import team6.finalproject.domain.follow.entity.Follow;
import team6.finalproject.domain.follow.repository.FollowRepository;
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.TargetType;
import team6.finalproject.domain.notification.event.NotificationCreatedEvent;
import team6.finalproject.domain.notification.repository.NotificationRepository;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public FollowDto create(Long followerId, FollowRequest request) {

    if (followerId.equals(request.followeeId())) {
      throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
    }

    if (followRepository.existsByFollowerIdAndFolloweeId(followerId, request.followeeId())) {
      throw new IllegalArgumentException("이미 팔로우하였습니다.");
    }

    User follower = userRepository.findById(followerId)
        .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

    User followee = userRepository.findById(request.followeeId())
        .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

    Follow follow = new Follow(follower, followee);
    followRepository.save(follow);

    Notification notification = new Notification(
        followee,
        "FOLLOW",
        follower.getName() + "님이 회원님을 팔로우했습니다.",
        Level.INFO,
        follower.getId(),
        TargetType.FOLLOWED_BY_USER
    );

    Notification saved = notificationRepository.save(notification);
    NotificationDto dto = NotificationDto.from(saved);
    eventPublisher.publishEvent(new NotificationCreatedEvent(dto));

    return FollowDto.from(follow);
  }


  @Transactional(readOnly = true)
  public boolean isFollowing(Long followerId, Long followeeId) {
    return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
  }

  @Transactional(readOnly = true)
  public long getFollowerCount(Long followeeId) {
    return followRepository.countByFolloweeId(followeeId);
  }

  @Transactional
  public void delete(Long followerId, Long followId) {
    Follow follow = followRepository.findById(followerId)
        .orElseThrow(() -> new NoSuchElementException("팔로우가 존재하지 않습니다."));

    if (!follow.getFollower().getId().equals(followerId)) {
      throw new IllegalArgumentException("본인의 팔로우만 취소할 수 있습니다");
    }

    followRepository.delete(follow);
  }


}

package team6.finalproject.domain.follow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.domain.follow.dto.FollowDto;
import team6.finalproject.domain.follow.dto.FollowRequest;
import team6.finalproject.domain.follow.service.FollowService;
import team6.finalproject.global.security.jwt.CustomUserDetails;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/follows")
public class FollowController {

  private final FollowService followService;

  @PostMapping
  public ResponseEntity<FollowDto> create(@RequestBody FollowRequest request) {
//    long t0 = System.nanoTime();
    FollowDto followDto = followService.create(currentUserId(), request);
//    long totalMs = (System.nanoTime() - t0) / 1_000_000;
//    log.info("[FOLLOW][SYNC] TOTAL={}ms follower={} followee={}",
//        totalMs, request.followeeId(), request.followeeId()
//    );


    return ResponseEntity.status(HttpStatus.CREATED).body(followDto);
  }

  @GetMapping("/followed-by-me")
  public ResponseEntity<Boolean> isFollowing(@RequestParam Long followeeId) {
    boolean following = followService.isFollowing(currentUserId(), followeeId);
    return ResponseEntity.status(HttpStatus.OK).body(following);
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getFollowerCount(@RequestParam Long followeeId) {
    long count = followService.getFollowerCount(followeeId);
    return ResponseEntity.status(HttpStatus.OK).body(count);
  }

  @DeleteMapping("/{followId}")
  public ResponseEntity<Void> delete(@PathVariable Long followId) {
    followService.delete(currentUserId(), followId);
    return ResponseEntity.status(HttpStatus.OK).build();
  }


  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || auth.getPrincipal() == null) {
      throw new IllegalStateException("Authentication is null");
    }

    Object user = auth.getPrincipal();

    if (user instanceof CustomUserDetails mud && mud.getUserDto() != null) {
      return mud.getUserDto().id();
    }

    if (user instanceof Long userId) {
      return userId;
    }

    throw new IllegalStateException("Authentication is null");
  }

}

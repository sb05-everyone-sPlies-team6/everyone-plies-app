package team6.finalproject.domain.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team6.finalproject.domain.follow.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
  Boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
  long countByFolloweeId(Long followeeId);
}

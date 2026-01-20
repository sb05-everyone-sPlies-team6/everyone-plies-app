package team6.finalproject.domain.follow.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team6.finalproject.domain.follow.entity.Follow;
import team6.finalproject.domain.user.entity.User;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
  Boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
  long countByFolloweeId(Long followeeId);

  @Query("""
select f.follower
from Follow f
where f.followee.id = :followeeId
""")
  List<User> findFollowersByFolloweeId(Long followeeId);
}

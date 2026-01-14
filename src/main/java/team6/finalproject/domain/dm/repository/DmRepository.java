package team6.finalproject.domain.dm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import team6.finalproject.domain.dm.entity.Dm;

public interface DmRepository extends JpaRepository<Dm, Long>, DmRepositoryCustom {

	@Query("SELECT cp.dmId FROM DmParticipant cp " +
		"WHERE cp.userId IN (:user1, :user2) " +
		"GROUP BY cp.dmId HAVING COUNT(cp.dmId) = 2")
	Optional<Long> findDmIdByParticipants(@Param("user1") Long user1, @Param("user2") Long user2);
}

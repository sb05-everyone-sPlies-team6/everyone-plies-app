package team6.finalproject.domain.dm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team6.finalproject.domain.dm.entity.DmParticipant;

import java.util.List;
import java.util.Optional;

public interface DmParticipantRepository extends JpaRepository<DmParticipant, Long> {

	// 특정 대화방에 속한 모든 참여자 조회
	List<DmParticipant> findAllByDmId(Long dmId);

	// 특정 대화방에서 '나'를 제외한 상대방 정보를 찾을 때 유용
	Optional<DmParticipant> findByDmIdAndUserIdNot(Long dmId, Long userId);

	// 유저가 특정 대화방의 참여자인지 확인
	boolean existsByDmIdAndUserId(Long dmId, Long userId);
}
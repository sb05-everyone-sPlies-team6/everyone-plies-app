package team6.finalproject.domain.dm.repository;

import java.util.List;
import java.util.Optional;

import team6.finalproject.domain.dm.dto.DmResponse;
import team6.finalproject.domain.dm.dto.MessageResponse;

public interface DmRepositoryCustom {
	//대화 목록 조회
	List<DmResponse> findDmListWithLastMessage(Long userId, String cursor, int limit, String keyword);
	//특정 대화 상세 조회
	Optional<DmResponse> findDmDetailById(Long dmId, Long currentUserId);
	//메시지 목록 조회
	List<MessageResponse> findMessagesByCursor(Long dmId, Long cursor, int limit);
}
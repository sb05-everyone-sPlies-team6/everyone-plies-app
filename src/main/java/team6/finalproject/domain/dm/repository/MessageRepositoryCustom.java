package team6.finalproject.domain.dm.repository;

import java.util.List;

import team6.finalproject.domain.dm.dto.MessageResponse;

public interface MessageRepositoryCustom {
	//DM 메시지 목록 조회 (커서 페이지네이션)
	List<MessageResponse> findMessagesByCursor(Long dmId, Long cursor, int limit);
}

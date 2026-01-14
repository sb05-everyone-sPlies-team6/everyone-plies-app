package team6.finalproject.domain.dm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.dm.dto.CursorResponse;
import team6.finalproject.domain.dm.dto.DirectMessageSendRequest;
import team6.finalproject.domain.dm.dto.DmResponse;
import team6.finalproject.domain.dm.dto.MessageResponse;
import team6.finalproject.domain.dm.dto.UserSimpleResponse;
import team6.finalproject.domain.dm.entity.Dm;
import team6.finalproject.domain.dm.entity.DmParticipant;
import team6.finalproject.domain.dm.entity.Message;
import team6.finalproject.domain.dm.repository.DmParticipantRepository;
import team6.finalproject.domain.dm.repository.MessageRepository;
import team6.finalproject.domain.dm.repository.DmRepository;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DmService {

	private final DmRepository dmRepository;
	private final DmParticipantRepository dmParticipantRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final SseService sseService;

	//대화방 생성 또는 조회
	@Transactional
	public DmResponse getOrCreateDm(Long currentUserId, Long withUserId) {
		// 1:1 대화방이 이미 있는지 확인
		Long dmId = dmRepository.findDmIdByParticipants(currentUserId, withUserId)
			.orElseGet(() -> createNewDm(currentUserId, withUserId));

		return getDmDetail(dmId, currentUserId);
	}

	private Long createNewDm(Long userA, Long userB) {
		Dm dm = dmRepository.save(Dm.create());
		dmParticipantRepository.save(new DmParticipant(dm.getId(), userA));
		dmParticipantRepository.save(new DmParticipant(dm.getId(), userB));
		return dm.getId();
	}

	//대화 목록 조회
	public List<DmResponse> getDmList(Long userId, String cursor, int limit, String keyword) {
		// Querydsl을 사용하여 유저가 참여 중인 Dm 목록과 마지막 메시지를 Slice로 가져옴
		return dmRepository.findDmListWithLastMessage(userId, cursor, limit, keyword);
	}

	//특정 대화 상세 조
	public DmResponse getDmDetail(Long dmId, Long currentUserId) {
		return dmRepository.findDmDetailById(dmId, currentUserId)
			.orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다."));
	}

	//DM 메시지 목록 조회
	public CursorResponse<MessageResponse> getMessages(Long dmId, Long cursor, int limit) {
		List<MessageResponse> fetchedMessages = dmRepository.findMessagesByCursor(dmId, cursor, limit);
		List<MessageResponse> messages = new ArrayList<>(fetchedMessages);

		boolean hasNext = false;
		if (messages.size() > limit) {
			messages.remove(limit);
			hasNext = true;
		}

		Long lastId = messages.isEmpty() ? null : messages.get(messages.size() - 1).id();

		return new CursorResponse<>(
			messages,
			lastId != null ? lastId.toString() : null,
			lastId,
			hasNext,
			0L,
			"createdAt",
			"DESCENDING"
		);
	}

	//DM 읽음 처리
	@Transactional
	public void markAllAsRead(Long dmId, Long currentUserId) {
		//DB 업데이트 (0 -> 1)
		messageRepository.markAllAsRead(dmId, currentUserId);

		// 프론트엔드 UI를 강제로 갱신시키기 위한 트리거.. 이거 말고 다른 로직 구상
		List<MessageResponse> messages = dmRepository.findMessagesByCursor(dmId, null, 1);
		if (!messages.isEmpty()) {
			MessageResponse latest = messages.get(0);
			sseService.sendDmNotification(currentUserId, latest);
		}
		sseService.sendReadNotification(currentUserId, dmId);
	}

	@Transactional
	public MessageResponse saveMessage(Long dmId, Long senderId, DirectMessageSendRequest request) {
		//상대방(수신자) 조회
		DmParticipant receiverParticipant = dmParticipantRepository.findByDmIdAndUserIdNot(dmId, senderId)
			.orElseThrow(() -> new IllegalArgumentException("대화 참여자를 찾을 수 없습니다."));
		Long receiverId = receiverParticipant.getUserId();

		//메시지 엔티티 생성 및 저장
		Message message = Message.builder()
			.dmId(dmId)
			.userId(senderId)
			.content(request.content())
			.isRead(false)
			.createdAt(LocalDateTime.now())
			.build();

		Message savedMessage = messageRepository.save(message);

		//발신자 및 수신자 정보 조회
		User sender = userRepository.findById(senderId)
			.orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
		User receiver = userRepository.findById(receiverId)
			.orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

		return new MessageResponse(
			savedMessage.getId(),
			savedMessage.getDmId(),
			savedMessage.getCreatedAt(),
			new UserSimpleResponse(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
			new UserSimpleResponse(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl()),
			savedMessage.getContent(),
			savedMessage.isRead()
		);
	}
}
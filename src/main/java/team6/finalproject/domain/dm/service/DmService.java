package team6.finalproject.domain.dm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import team6.finalproject.domain.notification.dto.NotificationDto;
import team6.finalproject.domain.notification.entity.Level;
import team6.finalproject.domain.notification.entity.Notification;
import team6.finalproject.domain.notification.entity.TargetType;
import team6.finalproject.domain.notification.repository.NotificationRepository;
import team6.finalproject.domain.sse.SseService;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DmService {

	private final DmRepository dmRepository;
	private final DmParticipantRepository dmParticipantRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final SseService sseService;
	private final RedisTemplate<String, Object> redisTemplate;
	private static final String DM_CACHE_KEY = "dm:cache:";

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
		String cacheKey = DM_CACHE_KEY + dmId;

		// [핵심 추가] 1. 첫 페이지 조회(cursor가 없을 때)는 Redis에서 먼저 가져오기
		if (cursor == null) {
			try {
				List<Object> cachedRaw = redisTemplate.opsForZSet()
					.reverseRange(cacheKey, 0, limit - 1)
					.stream().toList();

				if (cachedRaw != null && !cachedRaw.isEmpty()) {
					// Redis 데이터를 객체로 변환 (아까 수정한 Serializer 활용)
					List<MessageResponse> cachedMessages = cachedRaw.stream()
						.map(obj -> (MessageResponse) obj)
						.toList();

					return new CursorResponse<>(
						cachedMessages,
						cachedMessages.get(cachedMessages.size() - 1).id().toString(),
						cachedMessages.get(cachedMessages.size() - 1).id(),
						cachedMessages.size() >= limit,
						0L, "createdAt", "DESCENDING"
					);
				}
			} catch (Exception e) {
				log.error("Redis 캐시 조회 실패, DB로 전환: {}", e.getMessage());
			}
		}

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

		// '나'에게: 내 채팅방 목록의 "빨간 점"을 없애기 위한 신호만 전송
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

		User sender = userRepository.findById(senderId)
			.orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
		User receiver = userRepository.findById(receiverId)
			.orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

		MessageResponse response = new MessageResponse(
			savedMessage.getId(),
			savedMessage.getDmId(),
			savedMessage.getCreatedAt(),
			new UserSimpleResponse(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
			new UserSimpleResponse(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl()),
			savedMessage.getContent(),
			savedMessage.isRead()
		);

		sseService.sendDmNotification(receiverId, response);
		Notification notification = new Notification(
			receiver,
			"새로운 메시지",
			sender.getName() + "님이 메시지를 보냈습니다.",
			Level.INFO,
			dmId,
			TargetType.DM_RECEIVED
		);

		notificationRepository.save(notification);

		NotificationDto notificationDto = NotificationDto.from(notification);

		sseService.send(List.of(receiverId), "notifications", notificationDto);

		// Redis 캐시에 최근 메시지 추가
		String cacheKey = DM_CACHE_KEY + dmId;
		redisTemplate.opsForZSet().add(cacheKey, response, System.currentTimeMillis());
		redisTemplate.opsForZSet().removeRange(cacheKey, 0, -101);

		return response;
	}
}
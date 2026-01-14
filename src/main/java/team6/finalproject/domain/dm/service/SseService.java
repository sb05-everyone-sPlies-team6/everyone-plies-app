package team6.finalproject.domain.dm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team6.finalproject.domain.dm.dto.MessageResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {
	// 유저별 SseEmitter 저장소
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	//SSE 연결 (GET /api/sse)
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(60L * 1000 * 60); // 1시간 타임아웃
		emitters.put(userId, emitter);

		emitter.onCompletion(() -> emitters.remove(userId));
		emitter.onTimeout(() -> emitters.remove(userId));

		// 최초 연결 시 더미 데이터 전송 (503 에러 방지)
		try {
			emitter.send(SseEmitter.event().name("connect").data("connected"));
		} catch (IOException e) {
			emitters.remove(userId);
		}
		return emitter;
	}

	//DM 알림 전송
	public void sendDmNotification(Long receiverId, MessageResponse message) {
		SseEmitter emitter = emitters.get(receiverId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(message.id()))
					.name("direct-messages")
					.data(message));
			} catch (IOException e) {
				emitters.remove(receiverId);
			}
		}
	}

	public void sendReadNotification(Long userId, Long dmId) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name("direct-messages-read") // 읽음 처리 전용 이벤트 이름
					.data(dmId)); // 어떤 방이 읽혔는지 ID 전달
			} catch (IOException e) {
				emitters.remove(userId);
			}
		}
	}
}

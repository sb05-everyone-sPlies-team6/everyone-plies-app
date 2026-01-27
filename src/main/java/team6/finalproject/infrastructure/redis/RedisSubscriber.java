package team6.finalproject.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.dm.dto.MessageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			// Redis에서 온 메시지를 MessageResponse 객체로 역직렬화
			String publishMessage = new String(message.getBody());
			MessageResponse response = objectMapper.readValue(publishMessage, MessageResponse.class);

			// 해당 대화방을 구독 중인 클라이언트들에게 전송
			messagingTemplate.convertAndSend("/sub/conversations/" + response.conversationId() + "/direct-messages", response);

			log.info("Redis Pub/Sub 메시지 중계 완료: DM ID {}", response.conversationId());
		} catch (Exception e) {
			log.error("Redis 메시지 역직렬화 에러: {}", e.getMessage());
		}
	}
}
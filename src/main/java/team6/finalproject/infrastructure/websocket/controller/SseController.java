package team6.finalproject.infrastructure.websocket.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SseController {

    // 여러 구독자 관리
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // 타임아웃 없음
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        try {
            // 최초 연결 시 초기 이벤트 보내기
            emitter.send(SseEmitter.event().name("INIT").data("SSE 연결 성공"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    // 서버에서 메시지 발송용
    public void sendMessageToAll(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("MESSAGE").data(message));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}

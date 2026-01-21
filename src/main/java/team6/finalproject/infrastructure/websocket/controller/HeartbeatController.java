package team6.finalproject.infrastructure.websocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.infrastructure.websocket.manager.PresenceManager;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ws")
public class HeartbeatController {

    private final PresenceManager presenceManager;

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestParam String userId) {
        presenceManager.online(userId); // TTL 갱신
        return ResponseEntity.ok().build();
    }
}

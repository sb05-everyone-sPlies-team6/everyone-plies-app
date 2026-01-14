package team6.finalproject.domain.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team6.finalproject.global.security.MoplUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @RequestParam(value = "lastEventId",required = false) Long lastEventId
  ) {
    return sseService.connect(currentUserId(), lastEventId);
  }

  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || auth.getPrincipal() == null) {
      throw new IllegalStateException("Authentication is null");
    }

    Object user = auth.getPrincipal();

    if (user instanceof MoplUserDetails mud && mud.getUserDto() != null) {
      return mud.getUserDto().id();
    }

    if (user instanceof Long userId) {
      return userId;
    }

    throw new IllegalStateException("Authentication is null");
  }

}

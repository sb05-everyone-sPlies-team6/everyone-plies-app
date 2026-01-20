package team6.finalproject.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import team6.finalproject.global.security.jwt.JwtObject;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final ObjectMapper objectMapper; // 지금은 안 쓰면 지워도 됨
  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    // ✅ authentication이 null이어도 로그아웃되도록: Authorization 헤더에서 access 파싱
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String accessToken = authHeader.substring(7);

      try {
        JwtObject jwt = jwtTokenProvider.parseAccessToken(accessToken);
        Long userId = jwt.userDto().id();

        jwtRegistry.invalidateJwtInformationByUserId(userId);
        log.info("[LOGOUT] invalidated userId={}", userId);

      } catch (Exception e) {
        log.info("[LOGOUT] access token parse failed: {}", e.getMessage());
      }
    } else {
      log.info("[LOGOUT] no Authorization header");
    }

    // refresh 쿠키 삭제
    Cookie deleteRefreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN, null);
    deleteRefreshCookie.setPath("/");
    deleteRefreshCookie.setMaxAge(0);
    deleteRefreshCookie.setHttpOnly(true);
    // deleteRefreshCookie.setSecure(true); // HTTPS 운영이면 true 유지, 로컬 http면 주의
    response.addCookie(deleteRefreshCookie);

    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}

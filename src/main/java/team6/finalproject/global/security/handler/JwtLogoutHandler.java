package team6.finalproject.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import team6.finalproject.global.security.jwt.CustomUserDetails;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

// 토큰 파기 구현 필요 -> 완료

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final ObjectMapper objectMapper;
  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      Long userId = userDetails.getUserDto().id();
      jwtRegistry.invalidateJwtInformationByUserId(userId);
    }

      Cookie deleteRefreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN, null);
      deleteRefreshCookie.setPath("/");
      deleteRefreshCookie.setMaxAge(0);
      deleteRefreshCookie.setHttpOnly(true);
      response.addCookie(deleteRefreshCookie);

      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}

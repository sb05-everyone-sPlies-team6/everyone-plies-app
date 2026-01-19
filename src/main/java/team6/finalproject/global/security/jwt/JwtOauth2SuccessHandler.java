package team6.finalproject.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team6.finalproject.domain.user.dto.JwtInformation;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.global.security.MoplOauth2UserDetails;
import team6.finalproject.global.security.dto.ErrorResponse;

/**
 * OAuth2 로그인 성공 시:
 * 1) access/refresh 발급
 * 2) refresh 쿠키 저장
 * 3) registry 저장 (refresh 검증/회전용)
 * 4) 같은 서버(8080)의 메인(/)으로 redirect
 */
@Component
@RequiredArgsConstructor
public class JwtOauth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;

  /**
   * 선택 B: 백엔드가 프론트 정적 리소스를 서빙하므로 기본은 "/"
   * (필요 시 설정으로 변경 가능)
   */
  @Value("${auth.oauth2.redirect-uri:/}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    // OAuth principal 확인
    if (!(authentication.getPrincipal() instanceof MoplOauth2UserDetails oauthDetails)) {
      writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
      return;
    }

    try {
      // JWT 발급
      String accessToken = jwtTokenProvider.generateAccessToken(oauthDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(oauthDetails);

      // refresh 쿠키 저장
      Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
      response.addCookie(refreshCookie);

      // registry 저장
      UserDto userDto = UserDto.from(oauthDetails.getUser());
      jwtRegistry.registerJwtInformation(new JwtInformation(userDto, accessToken, refreshToken));

      // ✅ 메인 화면으로 이동 (static/index.html)
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.sendRedirect(redirectUri);

    } catch (Exception e) {
      writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private void writeJsonError(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(status);
    ErrorResponse errorResponse = new ErrorResponse(String.valueOf(status), message);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}

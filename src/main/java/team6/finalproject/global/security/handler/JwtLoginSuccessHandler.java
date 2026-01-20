package team6.finalproject.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team6.finalproject.domain.user.dto.JwtDto;
import team6.finalproject.domain.user.dto.JwtInformation;
import team6.finalproject.domain.user.dto.JwtUserResponse;
import team6.finalproject.global.security.jwt.CustomUserDetails;
import team6.finalproject.global.security.dto.ErrorResponse;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

//  로그인 성공 시 JWT 발급

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      try {
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
        response.addCookie(refreshCookie);

          JwtUserResponse userResponse = JwtUserResponse.from(userDetails.getUserDto());

          JwtDto jwtDto = new JwtDto(userResponse, accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        jwtRegistry.registerJwtInformation(
            new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken));
      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = new ErrorResponse(
            ""+HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      }
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ErrorResponse errorResponse = new ErrorResponse(
          ""+HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}

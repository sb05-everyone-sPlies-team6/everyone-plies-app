package team6.finalproject.global.security.oauth;

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
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;
import team6.finalproject.global.security.dto.ErrorResponse;

@Component
@RequiredArgsConstructor
public class JwtOauth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;


  @Value("${auth.oauth2.redirect-uri:/}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    if (!(authentication.getPrincipal() instanceof CustomOauth2UserDetails oauthDetails)) {
      writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
      return;
    }

    try {
      String accessToken = jwtTokenProvider.generateAccessToken(oauthDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(oauthDetails);

      Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
      response.addCookie(refreshCookie);

      UserDto userDto = UserDto.from(oauthDetails.getUser());
      jwtRegistry.registerJwtInformation(new JwtInformation(userDto, accessToken, refreshToken));

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

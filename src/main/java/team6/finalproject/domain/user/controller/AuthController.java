package team6.finalproject.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team6.finalproject.domain.user.dto.JwtDto;
import team6.finalproject.domain.user.dto.JwtInformation;
import team6.finalproject.domain.user.service.AuthService;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    log.debug("CRSF 토큰 조회");
    log.trace("CSRF 토큰: {}", csrfToken.getToken());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refreshToken(
      @CookieValue(JwtTokenProvider.REFRESH_TOKEN) String refreshToken,
      HttpServletResponse response
  ) {
    log.info("Refresh Token: {}", refreshToken);
    JwtInformation jwtInformation = authService.refreshToken(refreshToken);
    Cookie cookie = jwtTokenProvider.generateRefreshTokenCookie(jwtInformation.getRefreshToken());
    response.addCookie(cookie);

    JwtDto body = new JwtDto(jwtInformation.getUserDto(), jwtInformation.getAccessToken());
    return ResponseEntity.ok(body);
  }
}

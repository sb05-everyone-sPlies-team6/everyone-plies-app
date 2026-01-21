package team6.finalproject.domain.user.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.user.dto.JwtInformation;
import team6.finalproject.domain.user.dto.ResetPasswordRequest;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.global.security.jwt.CustomUserDetails;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final MailService mailService;
  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  private static final String TEMP_PW_KEY_PREFIX = "pw:temp:";

  @Value("${security.temp-password.ttl-seconds:180}")
  private long expirationSeconds;

  // 토큰 재발급!!
  @Transactional
  public JwtInformation refreshToken(String refreshToken) {
    if (!tokenProvider.validateRefreshToken(refreshToken)
    || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    String username = tokenProvider.getUsernameFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if(userDetails == null) {
      throw new UsernameNotFoundException("Invalid username or password");
    }

    try {
      CustomUserDetails moplUserDetails = (CustomUserDetails) userDetails;
      String newAccessToken = tokenProvider.generateAccessToken(moplUserDetails);
      String newRefreshToken = tokenProvider.generateRefreshToken(moplUserDetails);

      JwtInformation newJwtInformation = new JwtInformation(
          moplUserDetails.getUserDto(),
          newAccessToken,
          newRefreshToken
      );
      jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);
      return newJwtInformation;
    } catch (Exception e) {
      throw new RuntimeException("INTERNAL_SERVER_ERROR");
    }
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    String email = request.email();

    if(!userRepository.existsByEmail(email)) {
      throw new RuntimeException("Invalid email");
    }

    String tempPassword = generateTempPassword();

    String key = TEMP_PW_KEY_PREFIX + email;
    redisTemplate.opsForValue().set(key, tempPassword, Duration.ofSeconds(expirationSeconds));
    mailService.sendMail(email, tempPassword, expirationSeconds);

  }

  private String generateTempPassword() {
    return "mopl" +  UUID.randomUUID().toString().substring(0, 8);
  }

}

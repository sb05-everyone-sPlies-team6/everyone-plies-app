package team6.finalproject.global.security.jwt;

import java.time.Instant;
import team6.finalproject.domain.user.dto.UserDto;

public record JwtObject(
    Instant issueTime,
    Instant expirationTime,
    UserDto userDto,
    String token
) {
  public boolean isExpired() {
    return expirationTime.isBefore(Instant.now());
  }
}

package team6.finalproject.global.security.jwt;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import team6.finalproject.domain.user.dto.JwtInformation;

@Primary
@Component
@RequiredArgsConstructor
public class RedisRegistry implements JwtRegistry {

  private final StringRedisTemplate redis;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String PREFIX = "jwt:";

  private static final String K_REFRESH_BY_USER = PREFIX + "refresh:user:";
  private static final String K_ACCESS_BY_REFRESH = PREFIX + "access:by_refresh:";
  private static final String K_ACCESS_ACTIVE = PREFIX + "access:active:";


  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    Long userId = jwtInformation.getUserDto().id();

    invalidateJwtInformationByUserId(userId);

    String accessToken = jwtInformation.getAccessToken();
    String refreshToken = jwtInformation.getRefreshToken();

    Duration accessTtl = extractTtlFromAccessToken(accessToken);
    Duration refreshTtl = extractTtlFromRefreshToken(refreshToken);

    redis.opsForValue().set(
        K_REFRESH_BY_USER + userId,
        refreshToken,
        refreshTtl
    );

    redis.opsForValue().set(
        K_ACCESS_BY_REFRESH + refreshToken,
        accessToken,
        refreshTtl
    );

    redis.opsForValue().set(
        K_ACCESS_ACTIVE + accessToken,
        "1",
        accessTtl
    );

  }

  @Override
  public void invalidateJwtInformationByUserId(Long userId) {
    String refresh = redis.opsForValue().get(K_REFRESH_BY_USER + userId);
    if (refresh == null) return;

    String access = redis.opsForValue().get(K_ACCESS_BY_REFRESH + refresh);

    redis.delete(K_REFRESH_BY_USER + userId);
    redis.delete(K_ACCESS_BY_REFRESH + refresh);

    if (access != null) {
      redis.delete(K_ACCESS_ACTIVE + access);
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(Long userId) {
    return Boolean.TRUE.equals(redis.hasKey(K_REFRESH_BY_USER + userId));
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return Boolean.TRUE.equals(redis.hasKey(K_ACCESS_ACTIVE + accessToken));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return Boolean.TRUE.equals(redis.hasKey(K_ACCESS_BY_REFRESH + refreshToken));
  }

  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {

    String oldAccess = redis.opsForValue().get(K_ACCESS_BY_REFRESH + refreshToken);
    if (oldAccess != null) {
      redis.delete(K_ACCESS_ACTIVE + oldAccess);
    }
    redis.delete(K_ACCESS_BY_REFRESH + refreshToken);
    registerJwtInformation(newJwtInformation);
  }

  @Override
  public void clearExpiredJwtInformation() {
    // ttl 자동 만료... 구현 x
  }

  private Duration extractTtlFromAccessToken(String token) {
    JwtObject jwt = jwtTokenProvider.parseAccessToken(token);
    return ttlFrom(jwt.expirationTime());
  }

  private Duration extractTtlFromRefreshToken(String token) {
    JwtObject jwt = jwtTokenProvider.parseRefreshToken(token);
    return ttlFrom(jwt.expirationTime());
  }

  private Duration ttlFrom(Instant exp) {
    long seconds = exp.getEpochSecond() - Instant.now().getEpochSecond();
    return Duration.ofSeconds(Math.max(seconds, 1));
  }
}

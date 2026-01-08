package team6.finalproject.global.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.global.security.MoplUserDetails;

// JWT 토큰 발급, 유효성 검사, 사용자 정보를 꺼내줌

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN = "refresh_token";

  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  private final String issuer;

  // 서명/검증 시 필요
  private final JWSSigner accessTokenSigner;
  private final JWSVerifier accessTokenVerifier;
  private final JWSSigner refreshTokenSigner;
  private final JWSVerifier refreshTokenVerifier;

  public JwtTokenProvider(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
      @Value("${security.jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds,
      @Value("${security.jwt.issuer}") String issuer
  ) throws JOSEException {

    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

    this.issuer = issuer;

    this.accessTokenExpirationMs = accessTokenValiditySeconds * 1000L;
    this.refreshTokenExpirationMs = refreshTokenValiditySeconds * 1000L;

    this.accessTokenSigner = new MACSigner(secretBytes);
    this.accessTokenVerifier = new MACVerifier(secretBytes);
    this.refreshTokenSigner = new MACSigner(secretBytes);
    this.refreshTokenVerifier = new MACVerifier(secretBytes);
  }

  public String generateAccessToken(MoplUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  public String generateRefreshToken(MoplUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  private String generateToken(MoplUserDetails userDetails, long accessTokenExpirationMs,
      JWSSigner signer, String tokenType) throws JOSEException {

    String tokenId = UUID.randomUUID().toString();
    UserDto user = userDetails.getUserDto();

    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(user.name())
        .jwtID(tokenId)
        .issuer(issuer)
        .claim("userId", user.id())
        .claim("type", tokenType)
        .claim("email", user.email())
        .claim("createdAt", user.createdAt().toString())
        .claim("profileImageUrl", user.profileImageUrl())
        .claim("locked", Boolean.TRUE.equals(user.locked()))
        .claim("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .issueTime(now)
        .expirationTime(expiration)
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256),
        claimsSet
    );

    signedJWT.sign(signer);
    String token = signedJWT.serialize();

    return token;
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token, accessTokenVerifier, "access");
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token, refreshTokenVerifier, "refresh");
  }

  private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(verifier)) {
        log.debug("JWT signature verification failed for {} token", expectedType);

        return false;
      }

      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        log.debug("JWT claims set verification failed for {} token", expectedType);
        return false;
      }

      Date expiration = (Date) signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expiration == null || expiration.before(new Date())) {
        log.debug("JWT claims set verification failed for {} token", expectedType);
        return false;
      }

      return true;
    } catch (Exception e) {
      log.debug("JWT {} token validation failed: {}", expectedType, e.getMessage());
      return false;
    }
  }

  public Cookie generateRefreshTokenCookie(String refreshToken) {
    Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000L));
    return cookie;
  }

  public JwtObject parseAccessToken(String token) {
    return parseInternal(token, "access");
  }

  public JwtObject parseRefreshToken(String token) {
    return parseInternal(token, "refresh");
  }

  private JwtObject parseInternal(String token, String expectedType) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      JWSVerifier verifier = "access".equals(expectedType)
          ? accessTokenVerifier : refreshTokenVerifier;

      if (!signedJWT.verify(verifier)) {
        throw new IllegalStateException("JWT signature verification failed for " + expectedType);
      }

      String actualType = claims.getStringClaim("type");
      if (!expectedType.equals(actualType)) {
        throw new IllegalStateException("JWT claims set verification failed for " + expectedType);
      }

      Date exp = claims.getExpirationTime();
      if (exp == null || exp.before(new Date())) {
        throw new IllegalStateException("JWT claims set verification failed for " + expectedType);
      }

      Long userId = claims.getLongClaim("userId");
      String username = claims.getSubject();
      String email = claims.getStringClaim("email");
      String profileImageUrl = claims.getStringClaim("profileImageUrl");
      Boolean locked = claims.getBooleanClaim("locked");
      String createdAtStr = claims.getStringClaim("createdAt");
      LocalDateTime createdAt = createdAtStr != null
          ? LocalDateTime.parse(createdAtStr)
          : null;

      Date issueTime = claims.getIssueTime();

      List<String> roles = claims.getStringListClaim("roles");
      Role primaryRole = Role.valueOf(roles.get(0).substring(5));

      UserDto userDto = new UserDto(
          userId,
          createdAt,
          email,
          username,
          profileImageUrl,
          primaryRole,
          locked != null ? locked : false
      );

      return new JwtObject(
          issueTime.toInstant(),
          exp.toInstant(),
          userDto,
          token
      );

    } catch (Exception e) {
        throw new IllegalArgumentException("JWT 파싱 실패: " + e.getMessage(), e);    }
  }
}

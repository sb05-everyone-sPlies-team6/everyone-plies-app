package team6.finalproject.global.security.jwt;

// 현재 유효한 JWT들을 서버에서 관리/기록

import team6.finalproject.domain.user.dto.JwtInformation;

public interface JwtRegistry {

  void registerJwtInformation(JwtInformation jwtInformation);

  void invalidateJwtInformationByUserId(Long userId);

  boolean hasActiveJwtInformationByUserId(Long userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

  void clearExpiredJwtInformation();

}

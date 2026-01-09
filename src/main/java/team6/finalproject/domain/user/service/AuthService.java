package team6.finalproject.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.user.dto.JwtInformation;
import team6.finalproject.global.security.MoplUserDetails;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  // 토큰 재발급!!
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
      MoplUserDetails moplUserDetails = (MoplUserDetails) userDetails;
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

}

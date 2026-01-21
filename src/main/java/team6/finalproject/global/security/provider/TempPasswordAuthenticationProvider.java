package team6.finalproject.global.security.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;
import team6.finalproject.global.security.jwt.CustomUserDetails;

@Component
@RequiredArgsConstructor
public class TempPasswordAuthenticationProvider implements AuthenticationProvider {

  private static final String TEMP_PW_KEY_PREFIX = "pw:temp:";

  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String email = authentication.getName();
    String rawPassword = authentication.getCredentials() == null
        ? null : authentication.getCredentials().toString();

    if (email == null || rawPassword == null) {
      throw new BadCredentialsException("Invalid email or password");
    }

    email = email.trim().toLowerCase();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("Invalid email"));

    if (Boolean.TRUE.equals(user.getLocked())) {
      throw new LockedException("User is locked: " + email);
    }

    String key = TEMP_PW_KEY_PREFIX + email;
    String tempPassword = redisTemplate.opsForValue().get(key);

    if (tempPassword == null) {
      return null;
    }

    if (!passwordEncoder.matches(rawPassword, tempPassword)) {
      throw new BadCredentialsException("Invalid password");
    }

    CustomUserDetails userDetails = new CustomUserDetails(UserDto.from(user),
        user.getPassword());

    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());


  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}

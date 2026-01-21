package team6.finalproject.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

// DB에서 유저를 찾아 UserDetails로 바꿔줌

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("User with username " + username + " not found"));

    if (Boolean.TRUE.equals(user.getLocked())) {
      throw new LockedException("Account is locked");
    }

    UserDto userDto = new UserDto(user.getId(), user.getCreatedAt(),
        user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getRole(), user.getLocked());
    return new CustomUserDetails(userDto, user.getPassword());
  }
}

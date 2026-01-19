package team6.finalproject.global.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import team6.finalproject.domain.user.entity.User;

public class MoplOauth2UserDetails implements UserDetails, OAuth2User {

  private final User user;
  private Map<String, Object> attributes;

  public MoplOauth2UserDetails(User user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return user.getEmail();
  }

  public User getUser() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new GrantedAuthority() {
      @Override
      public String getAuthority() {
        return user.getRole().name();
      }
    });
    return authorities;
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

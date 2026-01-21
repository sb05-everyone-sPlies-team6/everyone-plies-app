package team6.finalproject.global.security.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import team6.finalproject.domain.user.entity.User;
import team6.finalproject.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();

    OAuth2UserInfo oAuth2UserInfo = null;

    if (provider.equals("google")) {
      oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes());
    } else if (provider.equals("kakao")) {
      oAuth2UserInfo = new KaKaoUserDetails(oAuth2User.getAttributes());

    } else {
      throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
    }

    String providerId = oAuth2UserInfo.getProviderId();
    String rawEmail = oAuth2UserInfo.getEmail();

    final String email = (rawEmail == null || rawEmail.isBlank())
        ? "user_" + oAuth2UserInfo.getProviderId() + "@kakao.com"
        : rawEmail;

    String name = oAuth2UserInfo.getName();

    User user = userRepository.findByProviderAndProviderId(provider, providerId)
        .orElseGet(() -> userRepository.save(new User(email, name, provider, providerId)));

    return new CustomOauth2UserDetails(user, oAuth2User.getAttributes());

  }

}

package team6.finalproject.global.security;

import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KaKaoUserDetails implements OAuth2UserInfo {

  private Map<String, Object> attributes;

  @Override
  public String getProvider() {
    return "kakao";
  }

  @Override
  public String getProviderId() {
    return attributes.get("id").toString();
  }

  @Override
  public String getEmail() {
    Map<String, Object> account =
        (Map<String, Object>) attributes.get("kakao_account");

    if (account == null) return null;

    return (String) account.get("email");
  }

  @Override
  public String getName() {
    return (String) ((Map) attributes.get("properties")).get("nickname");
  }
}

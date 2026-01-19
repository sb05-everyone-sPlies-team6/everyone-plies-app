package team6.finalproject.global.security;

public interface OAuth2UserInfo {
  String getProvider();
  String getProviderId();
  String getEmail();
  String getName();
}

//package team6.finalproject.global.security.jwt;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import team6.finalproject.domain.user.dto.SignInRequest;
//
//@RequiredArgsConstructor
//public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//  private final ObjectMapper objectMapper;
//
//  @Override
//  public Authentication attemptAuthentication(HttpServletRequest request,
//      HttpServletResponse response) throws AuthenticationException {
//    if (!request.getMethod().equals("POST")) {
//      throw new AuthenticationServiceException(
//          "Authentication method not supported: " + request.getMethod());
//    }
//    try {
//      // JSON 파싱
//      SignInRequest signInRequest = objectMapper.readValue(request.getInputStream(),
//          SignInRequest.class);
//
//      UsernamePasswordAuthenticationToken authenticationToken
//          = new UsernamePasswordAuthenticationToken(signInRequest.username(), signInRequest.password());
//
//      setDetails(request, authenticationToken);
//      return this.getAuthenticationManager().authenticate(authenticationToken); // 실제 인증이 발생하는 구간
//    } catch (Exception e) {
//      throw new AuthenticationServiceException("인증 실패! ", e);
//    }
//  }
//
//  public static class Configurer extends
//      AbstractAuthenticationFilterConfigurer<HttpSecurity, Configurer, JsonUsernamePasswordAuthenticationFilter> {
//
//    // 기본 로그인 처리 URL
//    public static final String DEFAULT_LOGIN_URL = "/api/auth/sign-in";
//
//    public Configurer(ObjectMapper objectMapper) {
//      // 두 번째 인자는 기본 loginProcessingUrl
//      super(new JsonUsernamePasswordAuthenticationFilter(objectMapper), DEFAULT_LOGIN_URL);
//    }
//
//    @Override
//    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
//      return request ->
//          request.getRequestURI().equals(loginProcessingUrl)
//              && request.getMethod().equals("POST");
//    }
//
//    @Override
//    public void init(HttpSecurity http) throws Exception {
//      loginProcessingUrl(DEFAULT_LOGIN_URL);
//      super.init(http);
//    }
//  }
//
//
//}

package team6.finalproject.global.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response)
      throws AuthenticationException {

    // 로그인은 무조건 POST만 허용
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      throw new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod());
    }

    // form-urlencoded 값 꺼내기
    String username = obtainUsername(request);
    String password = obtainPassword(request);

    if (username == null) username = "";
    if (password == null) password = "";

    username = username.trim();

    UsernamePasswordAuthenticationToken authRequest =
        new UsernamePasswordAuthenticationToken(username, password);

    setDetails(request, authRequest);

    return this.getAuthenticationManager().authenticate(authRequest);
  }
}


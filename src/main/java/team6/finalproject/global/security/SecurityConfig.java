package team6.finalproject.global.security;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import team6.finalproject.domain.user.entity.Role;
import team6.finalproject.global.security.jwt.InMemoryJwtRegistry;
import team6.finalproject.global.security.jwt.JsonUsernamePasswordAuthenticationFilter;
import team6.finalproject.global.security.jwt.JwtAuthenticationFilter;
import team6.finalproject.global.security.jwt.JwtLoginFailureHandler;
import team6.finalproject.global.security.jwt.JwtLoginSuccessHandler;
import team6.finalproject.global.security.jwt.JwtLogoutHandler;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      // ObjectMapper objectMapper,
      Http403ForbiddenAccessDeniedHandler accessDeniedHandler,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      JwtLogoutHandler jwtLogoutHandler,
      JwtLoginFailureHandler jwtLoginFailureHandler,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthenticationManager authenticationManager  // AuthenticationManager 주입
  ) throws Exception {

//     1) CSRF 설정 (쿠키 방식 + 일부 URL 예외)
    http.csrf(csrf -> csrf
        .ignoringRequestMatchers(
            "/api/auth/sign-in",     // 로그인은 CSRF 검사 제외
            "/api/auth/sign-out",    // 로그아웃도 제외
            "/api/auth/refresh" ,
            "/api/users",  // 토큰 리프레시도 제외
            "/api/sse"
        )
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
    );

    // 2) 인가(접근 권한) 설정
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/",               // 루트
            "/index.html",
            "/favicon.ico",
            "/vite.svg",
            "/assets/**",
            "/static/**",
            "/css/**",
            "/js/**",
            "/api/contents/**"
        ).permitAll()
        // 인증 관련 공개 엔드포인트
        .requestMatchers("/api/auth/csrf-token").permitAll()
        .requestMatchers("/api/auth/sign-in").permitAll()
        .requestMatchers("/api/auth/refresh").permitAll()
        .requestMatchers("/api/auth/reset-password").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입

        // Swagger / 문서 / H2 콘솔
        .requestMatchers(
            "/swagger-resource/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/**",
            "/assets/**",
            "/h2/**"
        ).permitAll()


        // WebSocket handshake는 허용
        .requestMatchers("/ws/**", "/ws").permitAll()

        // 어드민 API
        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
        .requestMatchers(
            HttpMethod.POST,
            "/api/users/{userId}/role",
            "/api/users/{userId}/locked"
        ).hasRole("ADMIN")

        // 그 외 나머지는 인증 필요
        .anyRequest().authenticated()
    );

    // 3) 세션 정책: 완전 Stateless (JWT 기반)
    http.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    // 4) formLogin 비활성화 (우리는 JSON 로그인만 사용)
    http.formLogin(AbstractHttpConfigurer::disable);

    // 5) JSON 로그인 필터 생성 & 설정
    JsonUsernamePasswordAuthenticationFilter jsonFilter =
        new JsonUsernamePasswordAuthenticationFilter();

    // 로그인 URL: POST /api/auth/sign-in (Swagger 스펙과 일치)
    jsonFilter.setFilterProcessesUrl("/api/auth/sign-in");
    jsonFilter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
    jsonFilter.setAuthenticationFailureHandler(jwtLoginFailureHandler);
    jsonFilter.setAuthenticationManager(authenticationManager);

    // UsernamePasswordAuthenticationFilter 자리에 JSON 필터 끼우기
    http.addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class);

    // 6) JWT 기반 로그아웃
    http.logout(logout -> logout
        .logoutUrl("/api/auth/sign-out")
        .addLogoutHandler(jwtLogoutHandler)
        .logoutSuccessHandler(
            new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
    );

    // 7) 예외 처리
    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
        .accessDeniedHandler(accessDeniedHandler)
    );

    // 8) 매 요청마다 Authorization 헤더의 JWT를 검증하는 필터
    http.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
    );

    // 9) CORS 설정
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
  }

//   CORS 설정
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(Arrays.asList("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  // 비밀번호 인코더
  @Bean
  public PasswordEncoder passwordEncoder() {
    return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    // return new BCryptPasswordEncoder();
  }

  // JWT 레지스트리 (동시 로그인 1개 허용)
  @Bean
  public JwtRegistry jwtRegistry(JwtTokenProvider jwtTokenProvider) {
    return new InMemoryJwtRegistry(1, jwtTokenProvider);
  }

  // 권한 계층: ADMIN ⊃ USER
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role(Role.ADMIN.name())
        .implies(Role.USER.name())
        .build();
  }
}

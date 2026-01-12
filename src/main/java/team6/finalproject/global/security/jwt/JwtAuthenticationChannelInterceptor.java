package team6.finalproject.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import team6.finalproject.global.security.MoplUserDetails;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final RoleHierarchy roleHierarchy;
    private final UserDetailsService userDetailsService;
    private final JwtRegistry jwtRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor)
                    .orElseThrow(() -> new RuntimeException("JWT token is missing"));

            // JWT 유효성 확인 + 활성 토큰 확인
            if (!tokenProvider.validateAccessToken(token) ||
                    !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
                log.debug("Invalid JWT token");
                throw new RuntimeException("JWT token is invalid or expired");
            }

            // username(email) 기반 UserDetails 조회
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // userId를 Principal로 세팅 (이 부분에서 username(email)을 principal로 설정)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities())
                    );
            accessor.setUser(authentication);

            log.debug("Set authentication for user: {}", username);
        }

        return message;
    }

    /**
     * STOMP 헤더에서 Authorization Bearer 토큰 추출
     */
    private Optional<String> resolveToken(StompHeaderAccessor accessor) {
        String prefix = "Bearer ";
        return Optional.ofNullable(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION))
                .map(value -> value.startsWith(prefix) ? value.substring(prefix.length()) : null);
    }
}


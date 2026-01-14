package team6.finalproject.infrastructure.websocket.interceptor;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import team6.finalproject.global.security.jwt.JwtRegistry;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final RoleHierarchy roleHierarchy;
    private final JwtRegistry jwtRegistry; // 활성 토큰 확인

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor)
                    .orElseThrow(() -> new RuntimeException("JWT token is missing"));

            if (!tokenProvider.validateAccessToken(token) ||
                    !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
                log.debug("Invalid JWT token");
                throw new RuntimeException("JWT token is invalid or expired");
            }

            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities())
            );

            // Principal 세팅
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("WebSocket authentication set for user: {}", username);
        }

        return message;
    }

    private Optional<String> resolveToken(StompHeaderAccessor accessor) {
        String prefix = "Bearer ";
        return Optional.ofNullable(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION))
                .map(value -> value.startsWith(prefix) ? value.substring(prefix.length()) : null);
    }
}

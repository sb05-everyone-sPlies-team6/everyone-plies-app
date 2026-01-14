package team6.finalproject.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import team6.finalproject.global.security.MoplUserDetails;
import team6.finalproject.global.security.jwt.JwtObject;
import team6.finalproject.global.security.jwt.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// 1. 웹소켓 연결(CONNECT) 시점에만 JWT 검증 진행
		if (StompCommand.CONNECT == accessor.getCommand()) {
			String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				String token = authorizationHeader.substring(7);

				try {
					// 2. 토큰 유효성 검사 (validateAccessToken 호출)
					if (jwtTokenProvider.validateAccessToken(token)) {

						// 3. 토큰 파싱 및 인증 객체 생성
						// JwtTokenProvider의 parseAccessToken은 JwtObject를 반환함
						JwtObject jwtObject = jwtTokenProvider.parseAccessToken(token);

						// 4. MoplUserDetails 생성 (비밀번호는 토큰에 없으므로 빈 문자열 처리)
						MoplUserDetails userDetails = new MoplUserDetails(jwtObject.userDto(), "");

						// 5. UsernamePasswordAuthenticationToken 생성
						Authentication authentication = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
						);

						// 6. STOMP 세션에 유저 정보 설정 (이 과정이 있어야 Controller에서 @AuthenticationPrincipal 작동)
						accessor.setUser(authentication);
						log.info("WebSocket 인증 성공: {}", userDetails.getUsername());
					}
				} catch (Exception e) {
					log.error("WebSocket JWT 인증 실패: {}", e.getMessage());
					// 연결 거부 로직이 필요하다면 여기서 Exception을 던질 수 있습니다.
				}
			}
		}
		return message;
	}
}
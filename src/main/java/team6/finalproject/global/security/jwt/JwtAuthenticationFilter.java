package team6.finalproject.global.security.jwt;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import team6.finalproject.domain.user.dto.UserDto;
import team6.finalproject.global.security.MoplUserDetails;
import team6.finalproject.global.security.dto.ErrorResponse;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final RoleHierarchy roleHierarchy;
  private final ObjectMapper objectMapper;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      String token = resolveToken(request);

      if (StringUtils.hasText(token)) {
        if (jwtTokenProvider.validateAccessToken(token)
            && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

          UserDto userDto = jwtTokenProvider.parseAccessToken(token).userDto();
          MoplUserDetails userDetails = new MoplUserDetails(userDto, null);

          if (!userDetails.isAccountNonLocked()) {
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, "Account is locked", HttpServletResponse.SC_FORBIDDEN);
            return;
          }

          var reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(
              userDetails.getAuthorities());

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null, reachableAuthorities);

          authentication.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request)
          );

          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          SecurityContextHolder.clearContext();
          sendErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
      }
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      sendErrorResponse(response, "JWT authentication failed", HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void sendErrorResponse(HttpServletResponse response, String message, int status)
      throws IOException {
    ErrorResponse errorResponse = new ErrorResponse(String.valueOf(status), message);

    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }
}

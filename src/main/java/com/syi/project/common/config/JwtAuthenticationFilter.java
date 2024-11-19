package com.syi.project.common.config;

import com.syi.project.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final CustomUserDetailsService userDetailsService;

  /**
   * HTTP 요청을 필터링하여 JWT 토큰을 검증하고 인증된 사용자를 SecurityContext에 설정
   *
   * @param request     HTTP 요청 객체
   * @param response    HTTP 응답 객체
   * @param filterChain 필터 체인
   * @throws ServletException, IOException 예외 발생 시
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String token = getTokenFromRequest(request);

    if (StringUtils.hasText(token) && jwtProvider.validateAccessToken(token)) {
      Long id = jwtProvider.getMemberPrimaryKeyId(token).orElse(null);

      if (id != null) {
        var userDetails = userDetailsService.loadUserByUsername(String.valueOf(id.toString()));
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("JWT 인증 성공 - 사용자 ID: {}", id);
      }
    } else {
      log.warn("유효하지 않은 JWT 토큰 요청 - IP: {}, URL: {}", request.getRemoteAddr(),
          request.getRequestURI());
    }

    filterChain.doFilter(request, response);
  }

  /**
   * 요청에서 Authorization 헤더를 통해 JWT 토큰을 추출
   *
   * @param request HTTP 요청 객체
   * @return 요청에서 추출된 JWT 토큰 (없으면 null 반환)
   */
  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

}

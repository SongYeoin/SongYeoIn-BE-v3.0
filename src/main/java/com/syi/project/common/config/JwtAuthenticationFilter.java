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
   * 특정 경로를 필터링하지 않도록 설정
   *
   * @param request HTTP 요청 객체
   * @return 필터링 제외 여부
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String requestURI = request.getRequestURI();
    return requestURI.equals("/favicon.ico") ||
        requestURI.startsWith("/swagger-ui") ||
        requestURI.startsWith("/v3/api-docs") ||
        requestURI.startsWith("/webjars") ||
        requestURI.equals("/") ||
        requestURI.startsWith("/admin/member/login") ||
        requestURI.startsWith("/member/login") ||
        requestURI.startsWith("/member/check-username") ||
        requestURI.startsWith("/member/check-email") ||
        requestURI.startsWith("/member/register") ||
        requestURI.startsWith("/refresh") ||
        requestURI.startsWith("/api/");
  }

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

    // 토큰 확인
    if (token == null) {
      log.warn("Authorization 헤더가 비어 있습니다. 요청 URL: {}", request.getRequestURI());
      setUnauthorizedResponse(response, "Authorization header is missing or invalid");
      return;
    }

    try {
      // JWT 토큰 검증
      if (StringUtils.hasText(token) && jwtProvider.validateAccessToken(token)) {
        jwtProvider.getMemberPrimaryKeyId(token).ifPresentOrElse(id -> {
          try {
            // 사용자 인증 설정
            var userDetails = userDetailsService.loadUserById(id);
            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT 인증 성공 - 사용자 ID: {}", id);
          } catch (Exception e) {
            log.error("사용자 로드 중 예외 발생: {}", e.getMessage());
          }
        }, () -> {
          log.warn("JWT에서 사용자 ID 추출 실패");
          setUnauthorizedResponse(response, "Invalid token payload");
        });
      } else {
        log.warn("유효하지 않은 JWT 토큰 요청 - IP: {}, URL: {}", request.getRemoteAddr(),
            request.getRequestURI());
        setUnauthorizedResponse(response, "Invalid or expired token");
        return;
      }
    } catch (Exception e) {
      log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
      setUnauthorizedResponse(response, "Token processing error");
      return;
    }

    // 필터 체인 진행
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

  /**
   * HTTP 응답을 401 Unauthorized로 설정
   */
  private void setUnauthorizedResponse(HttpServletResponse response, String message) {
    try {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      response.getWriter().write(message);
    } catch (IOException e) {
      log.error("응답 작성 중 오류 발생: {}", e.getMessage());
    }
  }

}

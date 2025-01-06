package com.syi.project.common.config;

import com.syi.project.auth.repository.JwtBlacklistRepository;
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
  private final JwtBlacklistRepository jwtBlacklistRepository;

  /**
   * 특정 경로를 필터링하지 않도록 설정
   *
   * @param request HTTP 요청 객체
   * @return 필터링 제외 여부
   */
  /*
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
        requestURI.startsWith("/refresh"); // Refresh API 경로 제외
  }
  */

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
      FilterChain filterChain) throws ServletException, IOException {

    String accessToken = getTokenFromRequest(request, "Authorization");
    String refreshToken = getTokenFromRequest(request, "Refresh-Token");

    // Access Token 처리
    if (accessToken != null) {
      if (isTokenBlacklisted(accessToken, "ACCESS")) {
        log.warn("블랙리스트에 등록된 Access Token 요청 차단 - Token: {}, URL: {}", accessToken, request.getRequestURI());
        setUnauthorizedResponse(response, "로그아웃된 Access Token입니다.");
        return;
      }

      processAccessToken(request, response, accessToken);
    }

    // Refresh Token 처리
    if (refreshToken != null) {
      if (isTokenBlacklisted(refreshToken, "REFRESH")) {
        log.warn("블랙리스트에 등록된 Refresh Token 요청 차단 - Token: {}", refreshToken);
        setUnauthorizedResponse(response, "로그아웃된 Refresh Token입니다.");
        return;
      }
      if (!jwtProvider.validateRefreshToken(refreshToken)) {
        log.warn("유효하지 않은 Refresh Token - Token: {}", refreshToken);
        setUnauthorizedResponse(response, "유효하지 않은 Refresh Token입니다.");
        return;
      }
    }

    // 필터 체인 진행
    filterChain.doFilter(request, response);
  }

  /**
   * Access Token 검증 및 인증 설정
   */
  private void processAccessToken(HttpServletRequest request, HttpServletResponse response, String accessToken) {
    if (!jwtProvider.validateAccessToken(accessToken)) {
      log.warn("유효하지 않은 Access Token 요청 - IP: {}, URL: {}", request.getRemoteAddr(), request.getRequestURI());
      return;
    }

    jwtProvider.getMemberPrimaryKeyId(accessToken).ifPresent(id -> {
      jwtProvider.getName(accessToken).ifPresent(name -> {
        jwtProvider.getRole(accessToken).ifPresent(role -> {
          try {
            var userDetails = userDetailsService.loadUserById(id);
            var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("JWT 인증 성공 - 사용자 ID: {}, 이름: {}, 역할: {}", id, name, role);
          } catch (Exception e) {
            log.error("사용자 로드 중 예외 발생: {}", e.getMessage());
          }
        });
      });
    });
  }

  /**
   * 요청에서 특정 헤더를 통해 JWT 토큰을 추출
   *
   * @param request HTTP 요청 객체
   * @param headerName 헤더 이름
   * @return 추출된 JWT 토큰 (없으면 null 반환)
   */
  private String getTokenFromRequest(HttpServletRequest request, String headerName) {
    String bearerToken = request.getHeader(headerName);
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
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(message);
    } catch (IOException e) {
      log.error("응답 작성 중 오류 발생: {}", e.getMessage());
    }
  }

  /**
   * JWT 토큰이 블랙리스트에 포함되어 있는지 확인
   *
   * @param token 검증할 JWT 토큰
   * @param tokenType 토큰 유형 (ACCESS/REFRESH)
   * @return 블랙리스트에 포함 여부
   */
  private boolean isTokenBlacklisted(String token, String tokenType) {
    String tokenId = jwtProvider.getJti(token);
    return tokenId != null && jwtBlacklistRepository.findByTokenIdAndTokenType(tokenId, tokenType).isPresent();
  }
}

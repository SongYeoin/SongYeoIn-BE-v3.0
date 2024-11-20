package com.syi.project.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.syi.project.auth.service.CustomUserDetailsService;
import com.syi.project.common.config.JwtAuthenticationFilter;
import com.syi.project.common.config.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationFilterTest {

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain filterChain;

  private final Long testUserId = 1L;
  private final String testUserRole = "ROLE_USER";
  private final String validToken = "ValidJwtToken";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = mock(FilterChain.class);
  }

  @Test
  @DisplayName("유효한 JWT 토큰 요청 - 필터 체인 정상 동작 테스트")
  void testDoFilterInternalWithValidToken() throws IOException, ServletException {
    // 요청에 유효한 Authorization 헤더 설정
    request.addHeader("Authorization", "Bearer " + validToken);

    // JwtProvider에서 토큰 검증과 ID 추출 동작을 모킹
    when(jwtProvider.validateAccessToken(validToken)).thenReturn(true);
    when(jwtProvider.getMemberPrimaryKeyId(validToken)).thenReturn(Optional.of(testUserId));

    // UserDetailsService에서 사용자 로드 동작을 모킹
    UserDetails userDetails = new User(
        String.valueOf(testUserId),
        "",
        Collections.singletonList(new SimpleGrantedAuthority(testUserRole))
    );
    when(userDetailsService.loadUserById(testUserId)).thenReturn(userDetails);

    // 필터 체인 수행
    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    // 필터 체인이 정상적으로 수행되었는지 검증
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰 요청 - 인증 실패")
  void testDoFilterInternalWithInvalidToken() throws IOException, ServletException {
    String invalidToken = "InvalidJwtToken";
    request.addHeader("Authorization", "Bearer " + invalidToken);

    when(jwtProvider.validateAccessToken(invalidToken)).thenReturn(false);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain, org.mockito.Mockito.never()).doFilter(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("Invalid or expired token", response.getContentAsString().trim());
  }
}

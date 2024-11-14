package com.syi.project.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.syi.project.auth.service.CustomUserDetailsService;
import com.syi.project.common.config.JwtProvider;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAccessControlIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtProvider jwtProvider;

  @MockBean
  private CustomUserDetailsService userDetailsService;

  private String accessToken;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Long testUserId = 1L;
    String testUserRole = "ROLE_USER";
    accessToken = jwtProvider.createAccessToken(testUserId, testUserRole);

    List<GrantedAuthority> authorities = Collections.singletonList(
        new SimpleGrantedAuthority(testUserRole));
    UserDetails userDetails = new User(String.valueOf(testUserId), "", authorities);

    when(userDetailsService.loadUserByUsername(String.valueOf(testUserId)))
        .thenReturn(userDetails);
  }

  @Test
  @DisplayName("유효한 토큰으로 보호된 엔드포인트 접근 성공 테스트")
  void testAccessProtectedEndpointWithValidToken() throws Exception {
    mockMvc.perform(get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("유효하지 않은 토큰으로 보호된 엔드포인트 접근 실패 테스트")
  void testAccessProtectedEndpointWithInvalidToken() throws Exception {
    String invalidToken = "InvalidToken";

    mockMvc.perform(get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("토큰 없이 보호된 엔드포인트 접근 실패 테스트")
  void testAccessProtectedEndpointWithoutToken() throws Exception {
    mockMvc.perform(get("/api/test")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}

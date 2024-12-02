/*
package com.syi.project.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.syi.project.common.config.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class JwtTest {

  private JwtProvider jwtProvider;
  private String secretKey;
  private long accessTokenValidity = 600000L;
  private long refreshTokenValidity = 1200000L;

  private Long testUserId = 1L;
  private String testUserRole = "ROLE_USER";
  private String accessToken;
  private String refreshToken;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256).toString();
    jwtProvider = new JwtProvider(secretKey, accessTokenValidity, refreshTokenValidity);
    accessToken = jwtProvider.createAccessToken(testUserId, testUserRole);
    refreshToken = jwtProvider.createRefreshToken(testUserId);
  }

  @Test
  @DisplayName("Access Token 생성 테스트")
  void testCreateAccessToken() {
    assertNotNull(accessToken);
    assertTrue(jwtProvider.validateAccessToken(accessToken));
  }

  @Test
  @DisplayName("Refresh Token 생성 테스트")
  void testCreateRefreshToken() {
    assertNotNull(refreshToken);
    assertTrue(jwtProvider.validateRefreshToken(refreshToken));
  }

  @Test
  @DisplayName("토큰에서 사용자 ID 추출 테스트")
  void testGetMemberPrimaryKeyId() {
    Optional<Long> extractedUserId = jwtProvider.getMemberPrimaryKeyId(accessToken);
    assertTrue(extractedUserId.isPresent());
    assertEquals(testUserId, extractedUserId.get());
  }

  @Test
  @DisplayName("토큰에서 역할(role) 추출 테스트")
  void testGetRole() {
    Optional<String> extractedRole = jwtProvider.getRole(accessToken);
    assertTrue(extractedRole.isPresent());
    assertEquals(testUserRole, extractedRole.get());
  }

  @Test
  @DisplayName("유효하지 않은 토큰 검증 테스트")
  void testInvalidToken() {
    String invalidToken = Jwts.builder().setSubject("invalid").compact();
    assertFalse(jwtProvider.validateAccessToken(invalidToken));
    assertFalse(jwtProvider.validateRefreshToken(invalidToken));
  }

  @Test
  @DisplayName("만료된 Access Token 검증 테스트")
  void testExpiredAccessToken() throws InterruptedException {
    long shortValidity = 1000L; // 1초 유효
    JwtProvider shortLivedJwtProvider = new JwtProvider(secretKey, shortValidity,
        refreshTokenValidity);
    String expiredToken = shortLivedJwtProvider.createAccessToken(testUserId, testUserRole);

    Thread.sleep(1500);

    assertFalse(shortLivedJwtProvider.validateAccessToken(expiredToken));
  }

  @Test
  @DisplayName("잘못된 서명 토큰 검증 테스트")
  void testTamperedToken() {
    String tamperedToken = accessToken + "tampered";
    assertFalse(jwtProvider.validateAccessToken(tamperedToken));
    assertFalse(jwtProvider.validateRefreshToken(tamperedToken));
  }

  @Test
  @DisplayName("만료된 Refresh Token 검증 테스트")
  void testExpiredRefreshToken() throws InterruptedException {
    long shortValidity = 1000L;
    JwtProvider shortLivedJwtProvider = new JwtProvider(secretKey, accessTokenValidity,
        shortValidity);
    String expiredRefreshToken = shortLivedJwtProvider.createRefreshToken(testUserId);

    Thread.sleep(1500);

    assertFalse(shortLivedJwtProvider.validateRefreshToken(expiredRefreshToken));
  }

  @Test
  @DisplayName("Null 또는 빈 문자열 토큰 검증 테스트")
  void testNullOrEmptyToken() {
    assertFalse(jwtProvider.validateAccessToken(null));
    assertFalse(jwtProvider.validateAccessToken(""));
    assertFalse(jwtProvider.validateRefreshToken(null));
    assertFalse(jwtProvider.validateRefreshToken(""));
  }

  @Test
  @DisplayName("Claim이 없는 토큰의 사용자 ID 및 역할 추출 실패 테스트")
  void testInvalidClaimsInToken() {
    String invalidToken = Jwts.builder().setSubject("invalid").compact();

    Optional<Long> memberId = jwtProvider.getMemberPrimaryKeyId(invalidToken);
    Optional<String> role = jwtProvider.getRole(invalidToken);

    assertTrue(memberId.isEmpty());
    assertTrue(role.isEmpty());
  }

  @Test
  @DisplayName("비정상적인 입력 처리 테스트")
  void testCreateTokenWithNullValues() {
    String tokenWithNullId = jwtProvider.createAccessToken(null, testUserRole);
    assertNotNull(tokenWithNullId);

    String tokenWithNullRole = jwtProvider.createAccessToken(testUserId, null);
    assertNotNull(tokenWithNullRole);
  }

  @Test
  @DisplayName("토큰에 올바른 Claim이 포함되는지 검증 테스트")
  void testTokenContainsCorrectClaims() {
    Optional<Long> extractedUserId = jwtProvider.getMemberPrimaryKeyId(accessToken);
    Optional<String> extractedRole = jwtProvider.getRole(accessToken);

    assertTrue(extractedUserId.isPresent());
    assertTrue(extractedRole.isPresent());
    assertEquals(testUserId, extractedUserId.get());
    assertEquals(testUserRole, extractedRole.get());
  }
}
*/

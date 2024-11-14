package com.syi.project.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtProvider {

  private final SecretKey key; // HMAC-SHA 알고리즘을 위한 비밀 키
  private final long accessTokenValidity; // Access Token 유효시간
  private final long refreshTokenValidity;  // Refresh Token 유효시간

  /**
   * 생성자 - JWT 시크릿키 및 유효시간 초기화
   *
   * @param secretKey            비밀키 값, 환경 변수나 설정 파일에서 주입
   * @param accessTokenValidity  Access Token 유효 시간
   * @param refreshTokenValidity Refresh Token 유효 시간
   */
  public JwtProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.accessTokenValidity}") long accessTokenValidity,
      @Value("${jwt.refreshTokenValidity}") long refreshTokenValidity
  ) {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
  }

  /**
   * Access Token 생성 메서드
   *
   * @param id   사용자 기본키 ID
   * @param role 사용자 역할
   * @return 생성된 Access Token 문자열
   */
  public String createAccessToken(Long id, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenValidity);

    log.info("Access Token 생성 시작 - 사용자 ID(기본키): {}, 역할: {}, 만료 시간: {}", id, role, validity);

    // JWT Access Token 생성
    String token = Jwts.builder()
        .setSubject(String.valueOf(id))         // 주체 설정
        .claim("role", role)                 // 역할 정보 추가
        .setIssuedAt(now)                       // 발급 시간 설정
        .setExpiration(validity)                // 만료 시간 설정
        .signWith(key, SignatureAlgorithm.HS256)  // 서명 알고리즘 및 키 설정
        .compact();                               // 최종 토큰 문자열 생성

    log.info("Access Token 생성 완료 - 사용자 ID(기본키): {}, 만료 시간: {}", id, validity);
    log.debug("Access Token - {}", token);
    return token;
  }

  /**
   * Refresh Token 생성
   *
   * @param id 사용자 기본키 ID
   * @return 생성된 Refresh Token 문자열
   */
  public String createRefreshToken(Long id) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenValidity);

    log.info("Refresh Token 생성 시작 - 사용자 ID(기본키): {}, 만료 시간: {}", id, validity);

    // JWT Refresh Token 생성
    String token = Jwts.builder()
        .setSubject(String.valueOf(id))         // 주체 설정
        .setIssuedAt(now)                       // 발급 시간 설정
        .setExpiration(validity)                // 만료 시간 설정
        .signWith(key, SignatureAlgorithm.HS256) // 서명 설정
        .compact();                             // Refresh Token 생성

    log.info("Refresh Token 생성 완료 - 사용자 ID(기본키): {}, 만료 시간: {}", id, validity);
    log.debug("Refresh Token - {}", token);
    return token;

  }

  /**
   * 토큰에서 사용자 기본키 ID 추출
   *
   * @param token JWT 토큰
   * @return Optional 로 감싼 사용자 기본키 ID (추출 실패 시 Optional.empty)
   */
  public Optional<Long> getMemberPrimaryKeyId(String token) {
    try {
      String memberId = getClaims(token).map(Claims::getSubject).orElse(null);
      log.info("토큰에서 사용자 기본키 ID 추출 성공 - 사용자 기본키 ID: {}", memberId);
      return Optional.of(Long.valueOf(memberId));
    } catch (Exception e) {
      log.error("토큰에서 사용자 ID 추출 실패 - 원인: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * 토큰에서 역할(role) 추출
   *
   * @param token JWT 토큰
   * @return Optional 로 감싼 사용자 역할 (추출 실패 시 Optional.empty)
   */
  public Optional<String> getRole(String token) {
    try {
      String role = getClaims(token).map(claims -> claims.get("role", String.class)).orElse(null);
      log.info("토큰에서 역할 정보 추출 성공 - 역할: {}", role);
      return Optional.ofNullable(role);
    } catch (Exception e) {
      log.error("토큰에서 역할 정보 추출 실패 - 원인: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Access Token의 유효성 검증
   *
   * @param token 검증할 Access Token 문자열
   * @return 유효 여부 (true: 유효, false: 유효하지 않음)
   */
  public boolean validateAccessToken(String token) {
    return validateToken(token, "AccessToken");
  }

  /**
   * Refresh Token의 유효성 검증
   *
   * @param token 검증할 Refresh Token 문자열
   * @return 유효 여부 (true: 유효, false: 유효하지 않음)
   */
  public boolean validateRefreshToken(String token) {
    return validateToken(token, "RefreshToken");
  }

  /**
   * JWT 토큰 유효성 검증 공통 메서드
   *
   * @param token     토큰 문자열
   * @param tokenType 토큰 유형 (Access/Refresh)
   * @return 유효 여부 (true: 유효, false: 유효하지 않음)
   */
  private boolean validateToken(String token, String tokenType) {
    try {
      // 토큰의 Claims 객체 추출 및 만료 시간 검증
      Claims claims = getClaims(token).orElseThrow();
      boolean isValid = !claims.getExpiration().before(new Date());
      log.info("{} 유효성 검증 결과 - 만료 시간: {}, 현재 시간: {}, 유효 여부: {}",
          tokenType, claims.getExpiration(), new Date(), isValid);
      return isValid;
    } catch (Exception e) {
      log.warn("유효하지 않은 {} - 원인:{}", tokenType, e.getMessage());
      return false;
    }
  }

  /**
   * JWT 토큰에서 Claims 추출
   *
   * @param token JWT 토큰
   * @return Optional 로 감싼 Claims 객체 (추출 실패 시 Optional.empty 반환)
   */
  private Optional<Claims> getClaims(String token) {
    try {
      // JWT 파서를 통해 토큰의 Claims 추출
      JwtParser parser = Jwts.parserBuilder()
          .setSigningKey(key)
          .build();
      Claims claims = parser.parseClaimsJws(token).getBody();
      log.info("토큰에서 Claims 추출 성공 - Claims: {}", claims);
      return Optional.of(claims);
    } catch (Exception e) {
      log.error("토큰에서 Claims 추출 실패 - 원인: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
package com.syi.project.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
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
   * @param id   사용자  기본키 ID
   * @param role 사용자 역할
   * @return 생성된 Access Token 문자열
   */
  public String createAccessToken(Long id, String name, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenValidity);
    String tokenId = UUID.randomUUID().toString();

    log.info("Access Token 생성 시작 - 사용자 ID: {}, 역할: {}, 만료 시간: {}", id, role, validity);

    // JWT Access Token 생성
    String token = Jwts.builder()
        .setId(tokenId) // jti 값 추가
        .setSubject(String.valueOf(id))          // 주체 설정
        .claim("name", name)                  // 사용자 이름
        .claim("role", role)                  // 사용자 역할
        .setIssuedAt(now)                        // 발급 시간 설정
        .setExpiration(validity)                // 만료 시간 설정
        .signWith(key, SignatureAlgorithm.HS256)  // 서명 알고리즘 및 키 설정
        .compact();                               // 최종 토큰 문자열 생성

    log.info("Access Token 생성 완료 - 사용자 ID: {}, 이름: {}, 만료 시간: {}", id, name, validity);
    log.debug("Access Token - 일부 정보: {}", token.substring(0, 10) + "...");
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
    String tokenId = UUID.randomUUID().toString();

    log.info("Refresh Token 생성 시작 - 사용자 ID(기본키): {}, 만료 시간: {}", id, validity);

    // JWT Refresh Token 생성
    String token = Jwts.builder()
        .setId(tokenId) // jti 값 추가
        .setSubject(String.valueOf(id))         // 주체 설정
        .setIssuedAt(now)                       // 발급 시간 설정
        .setExpiration(validity)                // 만료 시간 설정
        .signWith(key, SignatureAlgorithm.HS256) // 서명 설정
        .compact();                             // Refresh Token 생성

    log.info("Refresh Token 생성 완료 - 사용자 ID(기본키): {}, 만료 시간: {}", id, validity);
    log.debug("Refresh Token - 일부 정보: {}", token.substring(0, 10) + "...");
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
      Optional<String> idOptional = getClaims(token).map(Claims::getSubject);

      if (idOptional.isEmpty() || idOptional.get().isBlank()) {
        log.warn("토큰에서 사용자 기본키 ID 추출 실패 - subject가 null이거나 비어 있습니다.");
        return Optional.empty();
      }

      String id = idOptional.get();
      log.info("토큰에서 사용자 기본키 ID 추출 성공 - 사용자 기본키 ID: {}", id);

      try {
        return Optional.of(Long.valueOf(id));
      } catch (NumberFormatException e) {
        log.error("토큰에서 추출한 ID가 숫자 형식이 아닙니다. ID: {}", id, e);
        return Optional.empty();
      }
    } catch (Exception e) {
      log.error("토큰에서 사용자 ID 추출 실패 - 원인: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * 토큰에서 이름(name) 추출
   *
   * @param token JWT 토큰
   * @return Optional 로 감싼 사용자 이름 (추출 실패 시 Optional.empty)
   */
  public Optional<String> getName(String token) {
    try {
      String name = getClaims(token).map(claims -> claims.get("name", String.class)).orElse(null);
      log.info("토큰에서 이름 추출 성공 - 이름: {}", name);
      return Optional.ofNullable(name);
    } catch (Exception e) {
      log.error("토큰에서 이름 추출 실패 - 원인: {}", e.getMessage());
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
      Claims claims = getClaims(token).orElseThrow();
      boolean isExpired = claims.getExpiration().before(new Date());
      if (isExpired) {
        log.warn("{} 만료됨 - 만료 시간: {}, 현재 시간: {}", tokenType, claims.getExpiration(), new Date());
        return false;
      }
      log.info("{} 유효성 검증 성공 - 만료 시간: {}", tokenType, claims.getExpiration());
      return true;
    } catch (Exception e) {
      log.warn("유효하지 않은 {} - 원인: {}", tokenType, e.getMessage());
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
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      log.warn("토큰 만료 - 원인: {}", e.getMessage());
    } catch (io.jsonwebtoken.security.SecurityException e) {
      log.error("JWT 서명 검증 실패 - 원인: {}", e.getMessage());
    } catch (io.jsonwebtoken.MalformedJwtException e) {
      log.error("JWT 형식이 잘못됨 - 원인: {}", e.getMessage());
    } catch (Exception e) {
      log.error("토큰에서 Claims 추출 실패 - 원인: {}", e.getMessage());
    }
    return Optional.empty();
  }

  /**
   * JWT에서 jti (토큰 ID) 추출
   *
   * @param token JWT 토큰
   * @return jti 값
   */
  public String getJti(String token) {
    return getClaims(token)
        .map(Claims::getId)
        .orElse(null);
  }

  /**
   * JWT 만료 시간 반환 (LocalDateTime)
   *
   * @param token JWT 토큰
   * @return 만료 시간 (LocalDateTime)
   */
  public LocalDateTime getExpirationDate(String token) {
    return getClaims(token)
        .map(Claims::getExpiration)
        .map(exp -> exp.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
  }

}
package com.syi.project.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

  private final SecretKey key; // 비밀 키 객체
  private final long validityInMilliseconds; // 토큰 유효 시간

  // 비밀 키와 유효 시간 초기화
  public JwtProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.validity}") long validityInMilliseconds
  ) {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes()); // 시크릿 키 생성
    this.validityInMilliseconds = validityInMilliseconds;
  }

  // JWT 토큰 생성 메서드
  public String createToken(String memberId, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    JwtBuilder builder = Jwts.builder()
        .setSubject(memberId) // 사용자 ID 설정
        .claim("role", role) // 역할 추가
        .setIssuedAt(now) // 발급 시간 설정
        .setExpiration(validity) // 만료 시간 설정
        .signWith(key, SignatureAlgorithm.HS256); // 서명

    return builder.compact(); // 생성된 토큰 반환
  }

  // 토큰에서 사용자 ID 추출
  public String getMemberId(String token) {
    return getClaims(token).getSubject(); // Subject에서 memberId 추출
  }

  // 토큰에서 역할(role) 추출
  public String getRole(String token) {
    return getClaims(token).get("role", String.class);
  }

  // 토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      getClaims(token); // 토큰의 Claims를 파싱하여 유효성 확인
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // 토큰에서 Claims (정보) 추출
  private Claims getClaims(String token) {
    JwtParser parser = Jwts.parserBuilder()
        .setSigningKey(key) // 서명 키 설정
        .build();

    return parser.parseClaimsJws(token).getBody(); // Claims 파싱 후 반환
  }
}
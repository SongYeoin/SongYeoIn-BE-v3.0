package com.syi.project.auth.controller;

import com.syi.project.auth.dto.TokenInfoResponseDTO;
import com.syi.project.auth.dto.TokenRefreshResponseDTO;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.JwtService;
import com.syi.project.common.config.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "JWT 토큰 API", description = "JWT 토큰 관리 및 검증 기능")
public class JwtController {

  private final JwtProvider jwtProvider;
  private final JwtService jwtService;

  @GetMapping("/info")
  @Operation(summary = "토큰 정보 조회", description = "현재 토큰의 만료 시간과 상태 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 정보 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 없음")
  })
  public ResponseEntity<TokenInfoResponseDTO> getTokenInfo(
      @RequestHeader("Authorization") String authorization) {

    String token = extractToken(authorization);
    if (token == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      // 토큰 유효성 검증
      if (!jwtProvider.validateAccessToken(token)) {
        log.warn("유효하지 않은 토큰: {}", token);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      LocalDateTime expiryDate = jwtProvider.getExpirationDate(token);
      long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryDate);

      // 토큰 정보 응답 생성
      TokenInfoResponseDTO response = TokenInfoResponseDTO.builder()
          .expiryDate(expiryDate)
          .secondsRemaining(Math.max(0, secondsRemaining))
          .issuedAt(jwtProvider.getIssuedAt(token))
          .tokenId(jwtProvider.getJti(token))
          .build();

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("토큰 정보 조회 중 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/refresh")
  @Operation(summary = "토큰 갱신", description = "유효한 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 Refresh Token"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public ResponseEntity<TokenRefreshResponseDTO> refreshToken(
      @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
      @RequestHeader(value = "Refresh-Token", required = false) String headerRefreshToken,
      @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint,
      HttpServletRequest request,
      HttpServletResponse response) {

    log.info("토큰 갱신 요청 - 디바이스 지문: {}", deviceFingerprint != null ? "제공됨" : "없음");

    // 클라이언트 정보 수집
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = jwtService.getClientIp(request);

    // 쿠키 또는 헤더에서 Refresh Token 추출
    String refreshToken = cookieRefreshToken;
    if (refreshToken == null || refreshToken.isEmpty()) {
      refreshToken = extractToken(headerRefreshToken);
    }

    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new TokenRefreshResponseDTO(null, "Refresh Token이 제공되지 않았습니다."));
    }

    try {
      // 토큰 갱신 처리
      String newAccessToken = jwtService.refreshToken(refreshToken, userAgent, ipAddress, deviceFingerprint);

      // 캐시 방지 헤더 추가
      HttpHeaders headers = new HttpHeaders();
      headers.add("Cache-Control", "no-store");
      headers.add("Pragma", "no-cache");

      log.info("토큰 갱신 성공");
      return ResponseEntity.ok()
          .headers(headers)
          .body(new TokenRefreshResponseDTO(newAccessToken, "토큰이 성공적으로 갱신되었습니다."));
    } catch (Exception e) {
      log.error("토큰 갱신 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new TokenRefreshResponseDTO(null, e.getMessage()));
    }
  }

  @PostMapping("/revoke")
  @Operation(summary = "토큰 취소", description = "현재 Access Token을 블랙리스트에 추가하여 무효화합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 취소 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public ResponseEntity<Map<String, String>> revokeToken(
      @RequestHeader("Authorization") String authorization,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      HttpServletRequest request) {

    String token = extractToken(authorization);
    if (token == null || userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "유효한 토큰이 필요합니다."));
    }

    try {
      // 클라이언트 정보 수집
      String userAgent = request.getHeader("User-Agent");
      String ipAddress = jwtService.getClientIp(request);

      jwtService.revokeToken(token, userDetails.getId(), userAgent, ipAddress);

      Map<String, String> response = new HashMap<>();
      response.put("message", "토큰이 성공적으로 취소되었습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("토큰 취소 중 오류 발생: {}", e.getMessage());

      Map<String, String> response = new HashMap<>();
      response.put("message", "토큰 취소 중 오류가 발생했습니다: " + e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping("/validate")
  @Operation(summary = "토큰 유효성 검증", description = "Access Token의 유효성을 검증합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "유효한 토큰"),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
  })
  public ResponseEntity<Map<String, Object>> validateToken(
      @RequestHeader("Authorization") String authorization) {

    String token = extractToken(authorization);
    if (token == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "message", "토큰이 제공되지 않았습니다."));
    }

    try {
      // 토큰 유효성 검증
      boolean isValid = jwtProvider.validateAccessToken(token);
      Map<String, Object> response = new HashMap<>();

      if (isValid) {
        LocalDateTime expiryDate = jwtProvider.getExpirationDate(token);
        long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryDate);

        response.put("valid", true);
        response.put("expiryDate", expiryDate.toString());
        response.put("secondsRemaining", Math.max(0, secondsRemaining));
        response.put("message", "유효한 토큰입니다.");
      } else {
        response.put("valid", false);
        response.put("message", "유효하지 않은 토큰입니다.");
      }

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("토큰 유효성 검증 중 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "message", "토큰 검증 중 오류가 발생했습니다."));
    }
  }

  /**
   * Authorization 헤더에서 토큰 추출
   * @param bearerToken Bearer 토큰 문자열
   * @return 추출된 토큰 (없으면 null 반환)
   */
  private String extractToken(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
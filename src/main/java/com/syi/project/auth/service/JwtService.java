package com.syi.project.auth.service;

import com.syi.project.auth.entity.JwtBlacklist;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.entity.RefreshToken;
import com.syi.project.auth.repository.JwtBlacklistRepository;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.auth.repository.RefreshTokenRepository;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtBlacklistRepository jwtBlacklistRepository;

  /**
   * Refresh Token을 이용하여 새로운 Access Token을 발급
   * @param refreshToken Refresh Token
   * @param userAgent 사용자 브라우저 정보
   * @param ipAddress 사용자 IP 주소
   * @param deviceFingerprint 디바이스 지문 (프론트엔드에서 생성)
   * @return 새로 발급된 Access Token
   */
  @Transactional
  public String refreshToken(String refreshToken, String userAgent, String ipAddress, String deviceFingerprint) {
    log.info("Access Token 갱신 요청 - 사용자 IP: {}", ipAddress);

    // Refresh Token 유효성 검사
    if (!jwtProvider.validateRefreshToken(refreshToken)) {
      log.warn("유효하지 않은 Refresh Token: {}", refreshToken);
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // Refresh Token에서 사용자 ID 추출
    Optional<Long> idOpt = jwtProvider.getMemberPrimaryKeyId(refreshToken);
    if (idOpt.isEmpty()) {
      log.error("Refresh Token에서 사용자 ID 추출 실패");
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    Long id = idOpt.get();
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    // DB에 저장된 Refresh Token 조회
    Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findByMemberId(id);
    if (storedTokenOpt.isEmpty()) {
      log.warn("저장된 Refresh Token이 없음 - 사용자 ID: {}", id);
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    RefreshToken storedToken = storedTokenOpt.get();
    if (!storedToken.getToken().equals(refreshToken)) {
      log.warn("Refresh Token 불일치 - 요청된 토큰이 DB에 저장된 토큰과 다름");
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 토큰 탈취 감지 - 디바이스 정보나 IP 주소가 크게 다르면 의심
    if (!isTokenFromSameDevice(storedToken, userAgent, ipAddress)) {
      log.warn("토큰 탈취 의심 감지! memberId: {}, 저장된 정보와 요청 정보 불일치", id);
      // 모든 토큰 무효화 및 강제 로그아웃
      invalidateAllTokensForUser(id);
      throw new InvalidRequestException(ErrorCode.SECURITY_RISK_DETECTED);
    }

    // 디바이스 정보 업데이트
    storedToken.updateDeviceInfo(userAgent, ipAddress, extractDeviceInfo(userAgent));
    refreshTokenRepository.save(storedToken);

    // 새로운 Access Token 발급
    String newAccessToken = jwtProvider.createAccessToken(
        member.getId(), member.getName(), member.getRole().name(), deviceFingerprint);

    log.info("새로운 Access Token 발급 완료 - 사용자 ID: {}", id);
    return newAccessToken;
  }

  /**
   * Access Token을 블랙리스트에 추가하여 무효화
   * @param token 무효화할 Access Token
   * @param memberId 사용자 ID
   * @param userAgent 사용자 브라우저 정보
   * @param ipAddress 사용자 IP 주소
   */
  @Transactional
  public void revokeToken(String token, Long memberId, String userAgent, String ipAddress) {
    log.info("토큰 취소 요청 - 사용자 ID: {}", memberId);

    if (!jwtProvider.validateAccessToken(token)) {
      log.warn("취소 요청된 토큰이 이미 유효하지 않음");
      return;
    }

    String tokenId = jwtProvider.getJti(token);
    if (tokenId == null) {
      log.warn("토큰 ID(jti) 추출 실패");
      throw new InvalidRequestException(ErrorCode.INVALID_TOKEN);
    }

    // 이미 블랙리스트에 있는지 확인
    if (jwtBlacklistRepository.findByTokenIdAndTokenType(tokenId, "ACCESS").isPresent()) {
      log.info("이미 블랙리스트에 등록된 토큰 - Token ID: {}", tokenId);
      return;
    }

    LocalDateTime expiryDate = jwtProvider.getExpirationDate(token);

    // 블랙리스트에 추가 (디바이스 정보 포함)
    jwtBlacklistRepository.save(JwtBlacklist.builder()
        .tokenId(tokenId)
        .expiryDate(expiryDate)
        .tokenType("ACCESS")
        .userAgent(userAgent)
        .ipAddress(ipAddress)
        .deviceInfo(extractDeviceInfo(userAgent))
        .build());

    log.info("토큰 블랙리스트 추가 완료 - Token ID: {}, 만료 시간: {}", tokenId, expiryDate);
  }

  /**
   * 클라이언트 IP 주소 추출
   * @param request HTTP 요청
   * @return 클라이언트 IP 주소
   */
  public String getClientIp(HttpServletRequest request) {
    String clientIp = request.getHeader("X-Forwarded-For");
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("WL-Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_CLIENT_IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getRemoteAddr();
    }
    return clientIp;
  }

  /**
   * 디바이스 정보 추출
   * @param userAgent User-Agent 헤더 값
   * @return 디바이스 정보
   */
  public String extractDeviceInfo(String userAgent) {
    if (userAgent == null) return "unknown";

    String deviceInfo = "unknown";
    if (userAgent.contains("Mobile")) {
      deviceInfo = "Mobile";
    } else if (userAgent.contains("Tablet")) {
      deviceInfo = "Tablet";
    } else {
      deviceInfo = "Desktop";
    }

    // 운영체제 정보 추가
    if (userAgent.contains("Windows")) {
      deviceInfo += " - Windows";
    } else if (userAgent.contains("Mac")) {
      deviceInfo += " - Mac";
    } else if (userAgent.contains("Linux")) {
      deviceInfo += " - Linux";
    } else if (userAgent.contains("Android")) {
      deviceInfo += " - Android";
    } else if (userAgent.contains("iOS")) {
      deviceInfo += " - iOS";
    }

    return deviceInfo;
  }

  /**
   * 토큰이 같은 디바이스에서 온 것인지 확인
   * @param token 저장된 RefreshToken 객체
   * @param userAgent 현재 요청의 User-Agent
   * @param ipAddress 현재 요청의 IP 주소
   * @return 같은 디바이스에서 온 것으로 판단되면 true
   */
  private boolean isTokenFromSameDevice(RefreshToken token, String userAgent, String ipAddress) {
    if (token.getUserAgent() == null || token.getIpAddress() == null) {
      return true; // 기존 토큰에 정보가 없으면 검증 생략
    }

    // 디바이스 정보 비교 (완전 일치가 아닌 주요 부분 비교)
    boolean isSameDevice = token.getUserAgent().contains(extractDeviceInfo(userAgent));

    // IP 주소가 동일한지 확인 (동일 네트워크 내 IP 변경 가능성 고려)
    String storedIpBase = token.getIpAddress().substring(0, Math.min(token.getIpAddress().length(), 8));
    String currentIpBase = ipAddress.substring(0, Math.min(ipAddress.length(), 8));
    boolean isSameNetwork = storedIpBase.equals(currentIpBase);

    // 둘 중 하나만 일치해도 동일 사용자로 간주
    return isSameDevice || isSameNetwork;
  }

  /**
   * 사용자의 모든 활성 토큰 무효화
   * @param memberId 사용자 ID
   */
  @Transactional
  public void invalidateAllTokensForUser(Long memberId) {
    // Refresh Token 제거
    refreshTokenRepository.deleteByMemberId(memberId);
    log.info("보안 조치: 사용자 ID {}의 모든 Refresh Token 삭제", memberId);

    // 필요시 알림 메일 발송 등 추가 조치 구현
    memberRepository.findByIdAndDeletedByIsNull(memberId).ifPresent(member -> {
      log.warn("보안 조치: 사용자 ID {}({})의 의심스러운 로그인 시도 감지",
          memberId, member.getUsername());
      // TODO: 이메일 알림 등 추가 구현
    });
  }
}
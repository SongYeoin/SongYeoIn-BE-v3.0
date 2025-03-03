package com.syi.project.auth.repository;

import com.syi.project.auth.entity.JwtBlacklist;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

  /**
   * 토큰 ID와 타입으로 블랙리스트 항목 조회
   *
   * @param tokenId   JWT 토큰의 jti 값
   * @param tokenType 토큰 타입 (ACCESS/REFRESH)
   * @return 블랙리스트 항목
   */
  Optional<JwtBlacklist> findByTokenIdAndTokenType(String tokenId, String tokenType);

  /**
   * 특정 IP 주소에서 발행된 블랙리스트 토큰 조회
   *
   * @param ipAddress IP 주소
   * @return 블랙리스트 항목 목록
   */
  Iterable<JwtBlacklist> findByIpAddress(String ipAddress);

  /**
   * 만료된 블랙리스트 항목 삭제
   *
   * @param date 기준 날짜/시간
   * @return 삭제된 항목 수
   */
  @Modifying
  @Query("DELETE FROM JwtBlacklist b WHERE b.expiryDate < ?1")
  int deleteByExpiryDateBefore(LocalDateTime date);

  /**
   * 특정 사용자 에이전트로 발행된 블랙리스트 항목 조회 (보안 감사 및 의심스러운 활동 분석용)
   *
   * @param userAgent 사용자 에이전트 문자열
   * @return 블랙리스트 항목 목록
   */
  Iterable<JwtBlacklist> findByUserAgentContaining(String userAgent);
}

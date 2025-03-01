package com.syi.project.auth.repository;

import com.syi.project.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * 토큰 값으로 RefreshToken 조회
   *
   * @param token RefreshToken 값
   * @return Optional<RefreshToken>
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * 회원 ID로 RefreshToken 조회
   *
   * @param memberId 회원 ID
   * @return Optional<RefreshToken>
   */
  Optional<RefreshToken> findByMemberId(Long memberId);

  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshToken r WHERE r.memberId = :memberId")
  void deleteByMemberId(Long memberId);

  /**
   * 만료된 RefreshToken 삭제
   *
   * @param date 기준 날짜/시간
   * @return 삭제된 항목 수
   */
  @Modifying
  @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < ?1")
  int deleteByExpiryDateBefore(LocalDateTime date);

  /**
   * 특정 IP 주소에서 발급된 RefreshToken 찾기
   * (보안 감사 및 의심스러운 활동 분석용)
   *
   * @param ipAddress IP 주소
   * @return RefreshToken 목록
   */
  Iterable<RefreshToken> findByIpAddress(String ipAddress);

  /**
   * 특정 디바이스 정보로 발급된 RefreshToken 찾기
   *
   * @param deviceInfo 디바이스 정보
   * @return RefreshToken 목록
   */
  Iterable<RefreshToken> findByDeviceInfoContaining(String deviceInfo);



  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
  int deleteAllExpiredTokens(LocalDateTime now);
}

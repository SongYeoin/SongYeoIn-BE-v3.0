package com.syi.project.auth.repository;

import com.syi.project.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshToken r WHERE r.memberId = :memberId")
  void deleteByMemberId(Long memberId);

  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshToken r WHERE r.expiration < :now")
  int deleteAllExpiredTokens(LocalDateTime now);
}

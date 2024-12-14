package com.syi.project.auth.service;

import com.syi.project.auth.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public void cleanupExpiredTokens() {
    LocalDateTime nowKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    log.info("만료된 Refresh Token 삭제 작업 시작 - 현재 시각: {}", nowKST);
    int deletedCount = refreshTokenRepository.deleteAllExpiredTokens(nowKST);
    log.info("만료된 Refresh Token {}개 삭제 완료", deletedCount);
  }
}

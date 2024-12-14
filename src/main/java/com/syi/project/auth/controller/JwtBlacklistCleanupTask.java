package com.syi.project.auth.controller;

import com.syi.project.auth.repository.JwtBlacklistRepository;
import com.syi.project.auth.service.JwtBlacklistService;
import com.syi.project.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistCleanupTask {

  private final JwtBlacklistService jwtBlacklistService;
  private final RefreshTokenService refreshTokenService;

  @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
  public void runCleanupTask() {
    log.info("스케줄러 작업 시작");
    try {
      jwtBlacklistService.cleanupExpiredTokens();
      refreshTokenService.cleanupExpiredTokens();
      log.info("스케줄러 작업 완료");
    } catch (Exception e) {
      log.error("스케줄러 작업 중 예외 발생: {}", e.getMessage(), e);
    }
  }
}

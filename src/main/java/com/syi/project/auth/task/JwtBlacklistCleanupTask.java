package com.syi.project.auth.task;

import com.syi.project.auth.repository.JwtBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistCleanupTask {

  private final JwtBlacklistRepository jwtBlacklistRepository;

  @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
  public void cleanupExpiredTokens() {
    jwtBlacklistRepository.deleteAllByExpirationBefore(LocalDateTime.now());
    log.info("블랙리스트에서 만료된 토큰 삭제 완료");
  }
}

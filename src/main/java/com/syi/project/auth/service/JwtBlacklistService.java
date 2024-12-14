package com.syi.project.auth.service;

import com.syi.project.auth.repository.JwtBlacklistRepository;
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
public class JwtBlacklistService {

  private final JwtBlacklistRepository jwtBlacklistRepository;

  @Transactional
  public void cleanupExpiredTokens() {
    LocalDateTime nowKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    log.info("만료된 Access Token 삭제 작업 시작 - 현재 시각: {}", nowKST);
    int deletedCount = jwtBlacklistRepository.deleteAllByExpirationBefore(nowKST);
    log.info("만료된 Access Token {}개 삭제 완료", deletedCount);
  }

}

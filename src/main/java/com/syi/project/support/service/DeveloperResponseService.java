package com.syi.project.support.service;

import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.support.dto.DeveloperResponseDTO;
import com.syi.project.support.entity.DeveloperResponse;
import com.syi.project.support.entity.Support;
import com.syi.project.support.enums.SupportStatus;
import com.syi.project.support.repository.DeveloperResponseRepository;
import com.syi.project.support.repository.SupportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeveloperResponseService {

  private final DeveloperResponseRepository developerResponseRepository;
  private final SupportRepository supportRepository;

  @Value("${app.developer-api.key:dev-api-key-default}")
  private String apiKey;

  // ✅ API 키 유효성 검사
  public boolean validateApiKey(String requestApiKey) {
    return apiKey != null && apiKey.equals(requestApiKey);
  }

  // ✅ 디스코드에서 온 응답을 저장하고 상태 업데이트까지 처리
  public void handleDiscordResponse(Long supportId, String content, String developerId, String developerName) {
    createDeveloperResponse(supportId, content, developerId, developerName);

    if (content.contains("해결중")) {
      updateStatus(supportId, SupportStatus.IN_PROGRESS);
    } else if (content.contains("해결완료")) {
      updateStatus(supportId, SupportStatus.RESOLVED);
    }
  }

  // ✅ 답변 생성 (기존 답변 삭제 후 등록)
  public DeveloperResponseDTO createDeveloperResponse(Long supportId, String responseContent,
      String developerId, String developerName) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(supportId)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", supportId);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    developerResponseRepository.findBySupportId(supportId)
        .ifPresent(developerResponseRepository::delete);

    DeveloperResponse response = DeveloperResponse.builder()
        .supportId(supportId)
        .responseContent(responseContent)
        .developerId(developerId)
        .developerName(developerName)
        .build();

    developerResponseRepository.save(response);
    log.debug("개발팀 응답 저장 완료 - supportId: {}", supportId);

    return DeveloperResponseDTO.fromEntity(response);
  }

  // ✅ 상태 업데이트
  private void updateStatus(Long supportId, SupportStatus status) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(supportId)
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND));
    support.updateStatus(status);
    log.debug("문의 상태 업데이트: supportId={} → {}", supportId, status);
  }

  // ✅ 특정 문의의 답변 조회
  public DeveloperResponseDTO getDeveloperResponseBySupportId(Long supportId) {
    return developerResponseRepository.findBySupportId(supportId)
        .map(DeveloperResponseDTO::fromEntity)
        .orElse(null);
  }
}

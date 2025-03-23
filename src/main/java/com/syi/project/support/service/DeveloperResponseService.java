package com.syi.project.support.service;

import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.support.dto.DeveloperResponseDTO;
import com.syi.project.support.entity.DeveloperResponse;
import com.syi.project.support.entity.Support;
import com.syi.project.support.repository.DeveloperResponseRepository;
import com.syi.project.support.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeveloperResponseService {

  private final DeveloperResponseRepository developerResponseRepository;
  private final SupportRepository supportRepository;

  @Value("${app.developer-api.key:dev-api-key-default}")
  private String apiKey;

  public boolean validateApiKey(String requestApiKey) {
    return apiKey != null && apiKey.equals(requestApiKey);
  }

  @Transactional
  public DeveloperResponseDTO createDeveloperResponse(Long supportId, String responseContent,
      String developerId, String developerName) {
    // 문의글 존재 여부 확인
    Support support = supportRepository.findByIdAndDeletedByIsNull(supportId)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", supportId);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    // 기존 응답이 있는지 확인하고 삭제 (덮어쓰기 방식)
    developerResponseRepository.findBySupportId(supportId)
        .ifPresent(developerResponseRepository::delete);

    // 새 응답 저장
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

  public DeveloperResponseDTO getDeveloperResponseBySupportId(Long supportId) {
    return developerResponseRepository.findBySupportId(supportId)
        .map(DeveloperResponseDTO::fromEntity)
        .orElse(null);
  }
}
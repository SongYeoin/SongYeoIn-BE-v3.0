package com.syi.project.support.controller.api;

import com.syi.project.support.dto.DeveloperResponseDTO;
import com.syi.project.support.service.DeveloperResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/developer-responses")
@Tag(name = "개발팀 응답 API", description = "디스코드 봇 연동용 API")
public class DeveloperResponseApiController {

  private final DeveloperResponseService developerResponseService;

  @Operation(summary = "개발팀 응답 등록", description = "디스코드 봇에서 개발팀 응답을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "응답 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
  })
  @PostMapping("/support/{supportId}")
  public ResponseEntity<DeveloperResponseDTO> createDeveloperResponse(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long supportId,
      @RequestBody DeveloperResponseDTO requestDTO,
      @RequestHeader("X-API-KEY") String apiKey) {

    // API 키 검증
    if (!developerResponseService.validateApiKey(apiKey)) {
      log.warn("API 키 검증 실패 - supportId: {}", supportId);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    log.info("개발팀 응답 등록 - supportId: {}", supportId);
    DeveloperResponseDTO response = developerResponseService.createDeveloperResponse(
        supportId,
        requestDTO.getResponseContent(),
        requestDTO.getDeveloperId(),
        requestDTO.getDeveloperName()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "개발팀 응답 조회", description = "특정 문의에 대한 개발팀 응답을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "응답 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "응답을 찾을 수 없음")
  })
  @GetMapping("/support/{supportId}")
  public ResponseEntity<DeveloperResponseDTO> getDeveloperResponse(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long supportId,
      @RequestHeader("X-API-KEY") String apiKey) {

    // API 키 검증
    if (!developerResponseService.validateApiKey(apiKey)) {
      log.warn("API 키 검증 실패 - supportId: {}", supportId);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    DeveloperResponseDTO response = developerResponseService.getDeveloperResponseBySupportId(supportId);

    if (response == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }
}
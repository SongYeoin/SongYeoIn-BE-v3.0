package com.syi.project.support.controller;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.support.dto.SupportRequestDTO;
import com.syi.project.support.dto.SupportResponseDTO;
import com.syi.project.support.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/support")
@Tag(name = "수강생 고객센터 API", description = "고객센터 문의 관리 API")
public class SupportController {

  private final SupportService supportService;

  @Operation(summary = "문의 등록", description = "시스템 장애 문의를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping
  public ResponseEntity<SupportResponseDTO> createSupport(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "문의 요청 데이터", required = true) @Valid @RequestBody SupportRequestDTO requestDTO) {
    Long memberId = userDetails.getId();
    log.info("문의 등록 요청 - memberId: {}", memberId);
    log.info("문의 등록 요청 - Request DTO: {}", requestDTO);
    SupportResponseDTO support = supportService.createSupport(memberId, requestDTO);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "내 문의 목록 조회", description = "자신이 등록한 문의 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping
  public ResponseEntity<Page<SupportResponseDTO>> getMySupports(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20) Pageable pageable,
      @RequestParam(required = false) String keyword) {
    Long memberId = userDetails.getId();
    log.info("내 문의 목록 조회 요청 - memberId: {}, pageable: {}, keyword: {}", memberId, pageable, keyword);
    Page<SupportResponseDTO> supports = supportService.getMySupports(memberId, pageable, keyword);
    return ResponseEntity.ok(supports);
  }

  @Operation(summary = "문의 상세 조회", description = "문의 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping("/{id}")
  public ResponseEntity<SupportResponseDTO> getSupportDetail(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("문의 상세 조회 요청 - id: {}, memberId: {}", id, memberId);
    SupportResponseDTO support = supportService.getSupportDetail(id, memberId);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "문의 삭제", description = "문의를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "문의 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSupport(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("문의 삭제 요청 - id: {}, memberId: {}", id, memberId);
    supportService.deleteSupport(id, memberId);
    return ResponseEntity.noContent().build();
  }
}
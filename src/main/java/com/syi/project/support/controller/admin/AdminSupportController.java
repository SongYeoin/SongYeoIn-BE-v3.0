package com.syi.project.support.controller.admin;

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
@RequestMapping("/admin/support")
@Tag(name = "관리자 고객센터 API", description = "고객센터 문의 관리 API")
public class AdminSupportController {

  private final SupportService supportService;

  @Operation(summary = "관리자 문의 등록", description = "관리자가 시스템 장애 문의를 등록합니다.")
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
    log.info("관리자 문의 등록 - memberId: {}", memberId);
    SupportResponseDTO support = supportService.createSupport(memberId, requestDTO);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "문의 목록 조회", description = "모든 문의 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping
  public ResponseEntity<Page<SupportResponseDTO>> getAllSupports(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20) Pageable pageable,
      @RequestParam(required = false) String keyword) {
    log.info("전체 문의 목록 조회 - memberId: {}", userDetails.getId());
    Page<SupportResponseDTO> supports = supportService.getAllSupports(pageable, keyword);
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
    log.info("문의 상세 조회 - id: {}", id);
    SupportResponseDTO support = supportService.getSupportDetail(id, memberId);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "문의 확인 처리", description = "문의를 확인 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 확인 처리 성공"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PatchMapping("/{id}/confirm")
  public ResponseEntity<SupportResponseDTO> confirmSupport(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("문의 확인 처리 - id: {}", id);
    SupportResponseDTO support = supportService.confirmSupport(id, memberId);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "문의 확인 취소 처리", description = "문의 확인을 취소 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 확인 취소 처리 성공"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PatchMapping("/{id}/unconfirm")
  public ResponseEntity<SupportResponseDTO> unconfirmSupport(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("문의 확인 취소 처리 - id: {}", id);
    SupportResponseDTO support = supportService.unconfirmSupport(id, memberId);
    return ResponseEntity.ok(support);
  }

  @Operation(summary = "관리자 문의 삭제", description = "관리자가 작성한 문의를 삭제합니다.")
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
    log.info("관리자 문의 삭제 - id: {}", id);
    supportService.deleteSupport(id, memberId);
    return ResponseEntity.noContent().build();
  }
}
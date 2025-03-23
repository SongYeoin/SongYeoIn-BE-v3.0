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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SupportResponseDTO> createSupport(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "문의 제목", required = true) @RequestParam("title") String title,
      @Parameter(description = "문의 내용", required = true) @RequestParam("content") String content,
      @Parameter(description = "첨부 파일 (선택사항)") @RequestParam(value = "files", required = false) List<MultipartFile> files) {

    Long memberId = userDetails.getId();
    log.info("관리자 문의 등록 - memberId: {}", memberId);

    SupportRequestDTO requestDTO = SupportRequestDTO.builder()
        .title(title)
        .content(content)
        .files(files)
        .build();

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

  // "개발팀에게 전달" 기능을 추가
  @Operation(summary = "문의를 개발팀에게 전달", description = "문의를 디스코드를 통해 개발팀에게 전달합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "문의 전달 성공"),
      @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping("/{id}/send-to-dev")
  public ResponseEntity<Void> sendToDevTeam(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long id,
      @Parameter(description = "추가 메시지") @RequestBody(required = false) Map<String, String> request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    String additionalComment = request != null ? request.get("additionalComment") : null;

    log.info("문의를 개발팀에게 전달 - id: {}, 추가 메시지: {}", id, additionalComment);
    supportService.sendToDevTeam(id, memberId, additionalComment);
    return ResponseEntity.ok().build();
  }

  // 파일 다운로드
  @Operation(summary = "문의 첨부파일 다운로드", description = "문의에 첨부된 파일을 다운로드합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
      @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping("/{supportId}/files/{fileId}/download")
  public ResponseEntity<Resource> downloadSupportFile(
      @Parameter(description = "문의 ID", required = true) @PathVariable Long supportId,
      @Parameter(description = "파일 ID", required = true) @PathVariable Long fileId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    log.info("문의 첨부파일 다운로드 - supportId: {}, fileId: {}", supportId, fileId);

    return supportService.downloadSupportFile(supportId, fileId, memberId);
  }
}
package com.syi.project.journal.controller;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "수강생 교육일지 API", description = "수강생용 교육일지 API")
@Validated
public class JournalController {

  private final JournalService journalService;

  @Operation(summary = "교육일지 등록", description = "교육일지를 등록합니다. (파일 업로드 포함)")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JournalResponseDTO> createJournal(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @ModelAttribute JournalRequestDTO.Create requestDTO) {
    Long memberId = userDetails.getId();
    log.warn("교육일지 등록 - courseId: {}", requestDTO.getCourseId());
    return ResponseEntity.ok(journalService.createJournal(memberId, requestDTO));
  }

  @Operation(summary = "교육일지 목록 조회", description = "수강생 본인이 작성한 교육일지 목록을 조회합니다.")
  @GetMapping("/course/{courseId}")
  public ResponseEntity<Map<String, Object>> getJournalsList(
      @PathVariable Long courseId,
      @Valid @ModelAttribute Criteria criteria,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long memberId = userDetails.getId();

    Page<JournalResponseDTO> page = journalService.getStudentJournals(
        memberId,
        courseId,
        startDate,
        endDate,
        criteria
    );

    Map<String, Object> response = new HashMap<>();
    response.put("data", page.getContent());
    response.put("pageInfo", new PageInfoDTO(
        page.getTotalElements(),
        page.getTotalPages(),
        criteria.getPageNum(),
        criteria.getAmount(),
        page.hasNext(),
        page.hasPrevious()
    ));

    if (page.isEmpty()) {
      response.put("message", "검색 결과가 없습니다.");
    }

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "교육일지 상세 조회", description = "수강생 본인이 작성한 교육일지를 상세 조회합니다.")
  @GetMapping("/{journalId}")
  public ResponseEntity<JournalResponseDTO> getJournalDetail(
      @PathVariable Long journalId,
      @AuthenticationPrincipal CustomUserDetails userDetails // @AuthenticationPrincipal로 사용자 정보 주입
  ){
    Long memberId = userDetails.getId(); // userDetails에서 memberId 추출
    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
  }

  @Operation(summary = "교육일지 수정", description = "교육일지를 수정합니다. (파일 업로드 포함)")
  @PutMapping(value = "/{journalId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JournalResponseDTO> updateJournal(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long journalId,
      @Valid @ModelAttribute JournalRequestDTO.Update requestDTO) {
    Long memberId = userDetails.getId();
    log.warn("교육일지 수정 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.updateJournal(memberId, journalId, requestDTO));
  }

  @Operation(summary = "교육일지 삭제", description = "교육일지를 삭제합니다.")
  @DeleteMapping("/{journalId}")
  public ResponseEntity<Void> deleteJournal(
      @AuthenticationPrincipal CustomUserDetails userDetails, // @AuthenticationPrincipal로 사용자 정보 주입
      @PathVariable Long journalId) {
    Long memberId = userDetails.getId(); // userDetails에서 memberId 추출
    log.warn("교육일지 삭제 - journalId: {}, memberId: {}", journalId, memberId);
    journalService.deleteJournal(memberId, journalId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "교육일지 파일 다운로드", description = "수강생이 교육일지 파일을 다운로드합니다.")
  @GetMapping("/{journalId}/download")
  public ResponseEntity<Resource> downloadJournalFile(
      @PathVariable Long journalId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return journalService.downloadJournalFile(journalId, userDetails.getId());
  }
}
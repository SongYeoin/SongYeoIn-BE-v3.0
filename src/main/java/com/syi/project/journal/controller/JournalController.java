package com.syi.project.journal.controller;

import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "수강생 교육일지 API", description = "수강생용 교육일지 API")
public class JournalController {

  private final JournalService journalService;

  @Operation(summary = "교육일지 등록", description = "교육일지를 등록합니다. (파일 업로드 포함)")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JournalResponseDTO> createJournal(
      Long memberId,
      @Valid @ModelAttribute JournalRequestDTO requestDTO) {
    log.info("교육일지 등록 요청 - courseId: {}", requestDTO.getCourseId());
    return ResponseEntity.ok(journalService.createJournal(memberId, requestDTO));
  }

  @Operation(summary = "교육일지 목록 조회", description = "수강생 본인이 작성한 교육일지 목록을 조회합니다.")
  @GetMapping("/course/{courseId}")
  public ResponseEntity<List<JournalResponseDTO>> getJournalsList(
      @PathVariable Long courseId,
      Long memberId) {
    log.info("교육일지 목록 조회 요청 - courseId: {}, memberId: {}", courseId, memberId);
    return ResponseEntity.ok(journalService.getJournalsByCourse(courseId, memberId));
  }

  @Operation(summary = "교육일지 상세 조회", description = "수강생 본인이 작성한 교육일지를 상세 조회합니다.")
  @GetMapping("/{journalId}")
  public ResponseEntity<JournalResponseDTO> getJournalDetail(
      @PathVariable Long journalId,
      Long memberId) {
    log.info("교육일지 상세 조회 요청 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
  }

  @Operation(summary = "교육일지 수정", description = "교육일지를 수정합니다. (파일 업로드 포함)")
  @PutMapping(value = "/{journalId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JournalResponseDTO> updateJournal(
      Long memberId,
      @PathVariable Long journalId,
      @Valid @ModelAttribute JournalRequestDTO requestDTO) {
    log.info("교육일지 수정 요청 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.updateJournal(memberId, journalId, requestDTO));
  }

  @Operation(summary = "교육일지 삭제", description = "교육일지를 삭제합니다.")
  @DeleteMapping("/{journalId}")
  public ResponseEntity<Void> deleteJournal(
      Long memberId,
      @PathVariable Long journalId) {
    log.info("교육일지 삭제 요청 - journalId: {}, memberId: {}", journalId, memberId);
    journalService.deleteJournal(memberId, journalId);
    return ResponseEntity.noContent().build();
  }
}
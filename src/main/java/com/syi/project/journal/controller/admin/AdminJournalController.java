package com.syi.project.journal.controller.admin;

import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 교육일지 API", description = "관리자용 교육일지 API")
public class AdminJournalController {

  private final JournalService journalService;

  @Operation(summary = "[관리자] 교육일지 목록 조회", description = "관리자가 해당 강좌의 모든 교육일지를 조회합니다.")
  @GetMapping("/course/{courseId}")
  public ResponseEntity<List<JournalResponseDTO>> getJournalsList(
      @PathVariable Long courseId,
      Long memberId) {
    log.info("[관리자] 교육일지 목록 조회 요청 - courseId: {}, memberId: {}", courseId, memberId);
    return ResponseEntity.ok(journalService.getJournalsByCourse(courseId, memberId));
  }

  @Operation(summary = "[관리자] 교육일지 상세 조회", description = "관리자가 교육일지를 상세 조회합니다.")
  @GetMapping("/{journalId}")
  public ResponseEntity<JournalResponseDTO> getJournalDetail(
      @PathVariable Long journalId,
      Long memberId) {
    log.info("[관리자] 교육일지 상세 조회 요청 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
  }
}
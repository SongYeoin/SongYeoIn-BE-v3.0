package com.syi.project.journal.controller.admin;

import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
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
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 교육일지 API", description = "관리자용 교육일지 API")
@Validated
public class AdminJournalController {

  private final JournalService journalService;

  @Operation(summary = "[관리자] 교육일지 목록 조회",
      description = "관리자가 해당 강좌의 모든 교육일지를 조회합니다.")
  @GetMapping("/course/{courseId}")
  public ResponseEntity<Map<String, Object>> getJournalsList(
      @PathVariable Long courseId,
      @Positive(message = "올바른 회원 ID를 입력해주세요") Long memberId,
      @Valid @ModelAttribute Criteria criteria,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
  ) {
    log.info("[관리자] 교육일지 목록 조회 요청 - courseId: {}, criteria: {}",
        courseId, criteria);

    criteria.setAmount(20); // 페이지 사이즈 20으로 고정

    Page<JournalResponseDTO> page = journalService.searchJournals(criteria, memberId, startDate, endDate);

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

  @Operation(summary = "[관리자] 교육일지 상세 조회", description = "관리자가 교육일지를 상세 조회합니다.")
  @GetMapping("/{journalId}")
  public ResponseEntity<JournalResponseDTO> getJournalDetail(
      @PathVariable Long journalId,
      Long memberId) {
    log.info("[관리자] 교육일지 상세 조회 요청 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
  }
}
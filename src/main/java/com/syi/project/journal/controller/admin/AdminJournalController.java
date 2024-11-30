package com.syi.project.journal.controller.admin;

import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.journal.dto.JournalCourseResponseDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.course.dto.CourseResponseDTO;
import com.syi.project.journal.service.JournalService;
import com.syi.project.file.dto.FileDownloadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
      @RequestParam(required = false) String searchType,  // 추가
      @RequestParam(required = false) String searchKeyword,  // 추가
      @Valid @ModelAttribute Criteria criteria,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
  ) {
    log.info("[관리자] 교육일지 목록 조회 요청 - courseId: {}, searchType: {}, keyword: {}, criteria: {}",
        courseId, searchType, searchKeyword, criteria);

    criteria.setAmount(20); // 페이지 사이즈 20으로 고정

    Page<JournalResponseDTO> page = journalService.getAdminJournals(
        courseId,
        searchType,
        searchKeyword,
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

  @Operation(summary = "[관리자] 교육일지 상세 조회", description = "관리자가 교육일지를 상세 조회합니다.")
  @GetMapping("/{journalId}")
  public ResponseEntity<JournalResponseDTO> getJournalDetail(
      @PathVariable Long journalId,
      Long memberId) {
    log.info("[관리자] 교육일지 상세 조회 요청 - journalId: {}, memberId: {}", journalId, memberId);
    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
  }

  // 추가된 메서드: 교육과정 목록 조회
  @Operation(summary = "[관리자] 교육과정 목록 조회", description = "관리자가 활성화된 교육과정 목록을 조회합니다.")
  @GetMapping("/courses")
  public ResponseEntity<List<JournalCourseResponseDTO>> getActiveCourses() {
    log.info("[관리자] 교육과정 목록 조회 요청");
    return ResponseEntity.ok(journalService.getActiveCourses());
  }

  // 추가된 메서드: 교육일지 파일 다운로드
  @Operation(summary = "[관리자] 교육일지 파일 다운로드", description = "관리자가 교육일지 파일을 다운로드합니다.")
  @Parameter(name = "memberId", description = "회원 ID", required = true)
  @GetMapping("/{journalId}/download")
  public ResponseEntity<Resource> downloadJournalFile(
      @PathVariable Long journalId,
      //@AuthenticationPrincipal Long memberId
      @RequestParam Long memberId
  ) {
    log.info("[관리자] 교육일지 파일 다운로드 요청 - journalId: {}", journalId);
    return journalService.downloadJournalFile(journalId, memberId);
  }
}

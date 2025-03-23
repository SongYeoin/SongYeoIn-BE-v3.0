package com.syi.project.journal.controller.admin;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.file.service.FileService;
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
  private final FileService fileService;

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
    log.info("[관리자] 교육일지 목록 조회 - courseId: {}", courseId);

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
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("[관리자] 교육일지 상세 조회 - journalId: {}", journalId);
    return ResponseEntity.ok(journalService.getJournal(journalId, userDetails.getId()));
  }

  // 교육과정 목록 조회
  @Operation(summary = "[관리자] 교육과정 목록 조회", description = "관리자가 활성화된 교육과정 목록을 조회합니다.")
  @GetMapping("/courses")
  public ResponseEntity<List<JournalCourseResponseDTO>> getActiveCourses(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(journalService.getActiveCourses(userDetails.getId()));
  }

  // 교육일지 파일 다운로드
  @Operation(summary = "[관리자] 교육일지 파일 다운로드", description = "관리자가 교육일지 파일을 다운로드합니다.")
  @Parameter(name = "memberId", description = "회원 ID", required = true)
  @GetMapping("/{journalId}/download")
  public ResponseEntity<Resource> downloadJournalFile(
      @PathVariable Long journalId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return journalService.downloadJournalFile(journalId, userDetails.getId());
  }

  // zip 다운로드
  @Operation(summary = "[관리자] 교육일지 일괄 다운로드", description = "선택한 교육일지들을 ZIP 파일로 다운로드합니다.")
  @PostMapping("/zip-download")
  public ResponseEntity<Resource> downloadJournalsAsZip(
      @RequestBody List<Long> journalIds,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return journalService.downloadJournalsAsZip(journalIds, userDetails.getId());
  }

  // 파일이 S3에 존재하지 않는 것이 있는지 확인
  @PostMapping("/check-missing-files")
  public ResponseEntity<Map<String, Boolean>> checkMissingFiles(
      @RequestBody List<Long> journalIds,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("[관리자] 교육일지 파일 존재 확인 - journalIds: {}", journalIds);

    boolean hasMissingFiles = journalService.checkHasMissingFiles(journalIds, userDetails.getId());

    Map<String, Boolean> response = new HashMap<>();
    response.put("hasMissingFiles", hasMissingFiles);

    return ResponseEntity.ok(response);
  }
}
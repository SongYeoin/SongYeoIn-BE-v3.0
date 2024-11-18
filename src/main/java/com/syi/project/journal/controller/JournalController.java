//package com.syi.project.journal.controller;
//
//import com.syi.project.journal.dto.JournalRequestDTO;
//import com.syi.project.journal.dto.JournalResponseDTO;
//import com.syi.project.journal.service.JournalService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/journals")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Journal", description = "교육일지 API")
//public class JournalController {
//
//  private final JournalService journalService;
//
//  /**
//   * 교육일지 등록 (수강생만 가능)
//   * POST /api/journals
//   */
//  @Operation(summary = "교육일지 등록", description = "교육일지를 등록합니다. (파일 업로드 포함)")
//  @PreAuthorize("hasRole('STUDENT')")
//  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // 여기에 추가
//  public ResponseEntity<JournalResponseDTO> createJournal(
//      @AuthenticationPrincipal Long memberId,
//      @Valid @ModelAttribute JournalRequestDTO requestDTO) {
//    log.info("교육일지 등록 요청 - courseId: {}", requestDTO.getCourseId());
//    return ResponseEntity.ok(journalService.createJournal(memberId, requestDTO));
//  }
//
//  /**
//   * 교육일지 목록 조회
//   * - 수강생: 자신이 작성한 일지만 조회
//   * - 관리자: 특정 수업의 모든 일지 조회
//   * GET /api/journals/course/{courseId}
//   */
//  @Operation(
//      summary = "교육일지 목록 조회",
//      description = "수강생은 자신이 작성한 일지만, 관리자는 해당 강좌의 모든 일지를 조회할 수 있습니다."
//  )
//  @PreAuthorize("hasAnyRole('STUDENT', 'MANAGER')")
//  @GetMapping("/course/{courseId}")
//  public ResponseEntity<List<JournalResponseDTO>> getJournalsList(
//      @PathVariable Long courseId,
//      @AuthenticationPrincipal Long memberId) {
//    log.info("교육일지 목록 조회 요청 - courseId: {}, memberId: {}", courseId, memberId);
//    return ResponseEntity.ok(journalService.getJournalsByCourse(courseId, memberId));
//  }
//
//  /**
//   * 교육일지 상세 조회
//   * - 수강생: 자신이 작성한 일지
//   * - 관리자: 모든 일지
//   * GET /api/journals/{journalId}
//   */
//  @Operation(
//      summary = "교육일지 상세 조회",
//      description = "수강생은 자신이 작성한 일지만, 관리자는 모든 일지를 상세 조회할 수 있습니다."
//  )
//  @PreAuthorize("hasAnyRole('STUDENT', 'MANAGER')")
//  @GetMapping("/{journalId}")
//  public ResponseEntity<JournalResponseDTO> getJournalDetail(
//      @PathVariable Long journalId,
//      @AuthenticationPrincipal Long memberId) {
//    log.info("교육일지 상세 조회 요청 - journalId: {}, memberId: {}", journalId, memberId);
//    return ResponseEntity.ok(journalService.getJournal(journalId, memberId));
//  }
//
//  /**
//   * 교육일지 수정 (수강생만 가능)
//   * PUT /api/journals/{journalId}
//   */
//  @Operation(summary = "교육일지 수정", description = "교육일지를 수정합니다. (파일 업로드 포함)")
//  @PreAuthorize("hasRole('STUDENT')")
//  @PutMapping(value = "/{journalId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // 여기에 추가
//  public ResponseEntity<JournalResponseDTO> updateJournal(
//      @AuthenticationPrincipal Long memberId,
//      @PathVariable Long journalId,
//      @Valid @ModelAttribute JournalRequestDTO requestDTO) {
//    log.info("교육일지 수정 요청 - journalId: {}, memberId: {}", journalId, memberId);
//    return ResponseEntity.ok(journalService.updateJournal(memberId, journalId, requestDTO));
//  }
//
//  /**
//   * 교육일지 삭제 (수강생만 가능)
//   * DELETE /api/journals/{journalId}
//   */
//  @Operation(summary = "교육일지 삭제", description = "교육일지를 삭제합니다. (파일 업로드 포함)")
//  @PreAuthorize("hasRole('STUDENT')")
//  @DeleteMapping("/{journalId}")
//  public ResponseEntity<Void> deleteJournal(
//      @AuthenticationPrincipal Long memberId,
//      @PathVariable Long journalId) {
//    log.info("교육일지 삭제 요청 - journalId: {}, memberId: {}", journalId, memberId);
//    journalService.deleteJournal(memberId, journalId);
//    return ResponseEntity.noContent().build();
//  }
//}

package com.syi.project.journal.controller;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journal", description = "교육일지 API")
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

  @Operation(
      summary = "교육일지 목록 조회",
      description = "수강생은 자신이 작성한 일지만, 관리자는 해당 강좌의 모든 일지를 조회할 수 있습니다."
  )
  @GetMapping("/course/{courseId}")
  public ResponseEntity<List<JournalResponseDTO>> getJournalsList(
      @PathVariable Long courseId,
      Long memberId) {
    log.info("교육일지 목록 조회 요청 - courseId: {}, memberId: {}", courseId, memberId);
    return ResponseEntity.ok(journalService.getJournalsByCourse(courseId, memberId));
  }

  @Operation(
      summary = "교육일지 상세 조회",
      description = "수강생은 자신이 작성한 일지만, 관리자는 모든 일지를 상세 조회할 수 있습니다."
  )
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

  @Operation(summary = "교육일지 삭제", description = "교육일지를 삭제합니다. (파일 업로드 포함)")
  @DeleteMapping("/{journalId}")
  public ResponseEntity<Void> deleteJournal(
      Long memberId,
      @PathVariable Long journalId) {
    log.info("교육일지 삭제 요청 - journalId: {}, memberId: {}", journalId, memberId);
    journalService.deleteJournal(memberId, journalId);
    return ResponseEntity.noContent().build();
  }
}
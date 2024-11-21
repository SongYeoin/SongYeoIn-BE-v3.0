package com.syi.project.notice.controller;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.notice.dto.NoticeRequestDTO;
import com.syi.project.notice.dto.NoticeResponseDTO;
import com.syi.project.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/amdin/notice")
@Tag(name = "관리자 공지사항 API", description = "공지사항 API")
public class NoticeController {

  private final NoticeService noticeService;

  @Operation(summary = "공지사항 생성", description = "공지사항을 생성합니다. 첨부 파일을 포함할 수 있습니다.")
  @PostMapping
  public ResponseEntity<NoticeResponseDTO> createNotice(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") NoticeRequestDTO requestDTO,
      @RequestPart(value = "files", required = false) List<MultipartFile> files) {
    Long memberId = userDetails.getId();
    log.info("공지사항 생성 요청 - memberId: {}", memberId);
    NoticeResponseDTO notice = noticeService.createNotice(memberId, requestDTO, files);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 목록 조회", description = "교육과정 공지사항과 전체 공지사항을 조회합니다.")
  @GetMapping
  public ResponseEntity<List<NoticeResponseDTO>> getNotices(
      @RequestParam(required = false) Long courseId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("공지사항 목록 조회 요청 - courseId: {}, memberId: {}", courseId, memberId);
    List<NoticeResponseDTO> notices = noticeService.getNotices(courseId, memberId);
    return ResponseEntity.ok(notices);
  }

  @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 정보를 조회합니다.")
  @GetMapping("{id}")
  public ResponseEntity<NoticeResponseDTO> getNoticeDetail(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("공지사항 상세 조회 요청 - id: {}, memberId: {}", id, memberId);
    NoticeResponseDTO notice = noticeService.getNoticeDetail(id, memberId);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다.")
  @PutMapping("{id}")
  public ResponseEntity<NoticeResponseDTO> updateNotice(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("request") NoticeRequestDTO requestDTO,
      @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
      @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds
  ) {
    Long memberId = userDetails.getId();
    log.info("공지사항 수정 요청 - id: {}, memberId: {}", id, memberId);
    NoticeResponseDTO notice = noticeService.updateNotice(id, memberId, requestDTO, newFiles,
        deleteFileIds);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotice(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("공지사항 삭제 요청 - noticeId: {}, memberId: {}", id, memberId);
    noticeService.deleteNotice(id, memberId);
    return ResponseEntity.noContent().build();
  }
}

package com.syi.project.notice.controller.admin;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.notice.dto.NoticeRequestDTO;
import com.syi.project.notice.dto.NoticeResponseDTO;
import com.syi.project.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping("/admin/notice")
@Tag(name = "관리자 공지사항 API", description = "공지사항 관리 API")
public class AdminNoticeController {

  private final NoticeService noticeService;

  @Operation(summary = "공지사항 생성", description = "공지사항을 생성합니다. 첨부 파일을 포함할 수 있습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공지사항 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping
  public ResponseEntity<NoticeResponseDTO> createNotice(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "공지사항 요청 데이터", required = true) @Valid @RequestPart("request") NoticeRequestDTO requestDTO,
      @Parameter(description = "첨부 파일 목록") @RequestPart(value = "files", required = false) List<MultipartFile> files) {
    Long memberId = userDetails.getId();
    log.info("공지사항 생성 요청 - memberId: {}", memberId);
    log.info("공지사항 생성 요청 - Request DTO: {}", requestDTO);
    log.info("공지사항 생성 요청 - Files count: {}", files != null ? files.size() : 0);
    NoticeResponseDTO notice = noticeService.createNotice(memberId, requestDTO, files);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 목록 조회", description = "교육과정별 공지사항을 조회합니다. 상단고정 공지사항이 우선 표시됩니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping
  public ResponseEntity<Page<NoticeResponseDTO>> getNotices(
      @Parameter(description = "교육과정 ID") @RequestParam(required = true) Long courseId,
      @Parameter(description = "제목 키워드 필터") @RequestParam(required = false) String titleKeyword,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20) Pageable pageable) {
    Long memberId = userDetails.getId();
    log.info("공지사항 목록 조회 요청 - courseId: {}, memberId: {}, titleKeyword: {}, pageable: {}",
        courseId, titleKeyword, memberId, pageable);
    Page<NoticeResponseDTO> notices = noticeService.getNotices(courseId, memberId, titleKeyword,
        pageable);

    return ResponseEntity.ok(notices);
  }

  @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공지사항 상세 조회 성공"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping("{id}")
  public ResponseEntity<NoticeResponseDTO> getNoticeDetail(
      @Parameter(description = "공지사항 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("공지사항 상세 조회 요청 - id: {}, memberId: {}", id, memberId);
    NoticeResponseDTO notice = noticeService.getNoticeDetail(id, memberId);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공지사항 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PutMapping("{id}")
  public ResponseEntity<NoticeResponseDTO> updateNotice(
      @Parameter(description = "공지사항 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "공지사항 요청 데이터", required = true) @Valid @RequestPart("request") NoticeRequestDTO requestDTO,
      @Parameter(description = "새로운 첨부 파일 목록") @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
      @Parameter(description = "삭제할 첨부 파일 ID 목록") @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds
  ) {
    Long memberId = userDetails.getId();
    log.info("공지사항 수정 요청 - id: {}, memberId: {}", id, memberId);
    log.info("공지사항 수정 요청 - Request DTO: {}", requestDTO);
    log.info("공지사항 수정 요청 - newFiles count: {}", newFiles != null ? newFiles.size() : 0);
    log.info("공지사항 수정 요청 - deleteFileIds count: {}", deleteFileIds != null ? deleteFileIds.size() : 0);
    NoticeResponseDTO notice = noticeService.updateNotice(id, memberId, requestDTO, newFiles,
        deleteFileIds);
    return ResponseEntity.ok(notice);
  }

  @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "공지사항 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotice(
      @Parameter(description = "공지사항 ID", required = true) @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    log.info("공지사항 삭제 요청 - noticeId: {}, memberId: {}", id, memberId);
    noticeService.deleteNotice(id, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "공지사항 첨부 파일 다운로드", description = "공지사항에 첨부된 파일을 다운로드합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
      @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @GetMapping("/{id}/files/{fileId}/download")
  public ResponseEntity<Resource> downloadFile(
      @Parameter(description = "공지사항 ID", required = true) @PathVariable Long id,
      @Parameter(description = "파일 ID", required = true) @PathVariable Long fileId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    log.info("관리자 공지사항 파일 다운로드 요청 - id: {}, fileId: {}, memberId: {}", id, fileId, memberId);

    FileDownloadDTO downloadDTO = noticeService.downloadNoticeFile(id, fileId, memberId);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(downloadDTO.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + downloadDTO.getOriginalName() + "\"")
        .body(downloadDTO.getResource());
  }
}

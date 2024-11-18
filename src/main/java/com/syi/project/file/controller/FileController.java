package com.syi.project.file.controller;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 API")
public class FileController {
  private final FileService fileService;
  private final MemberRepository memberRepository;

  @Operation(summary = "파일 다운로드")
  @GetMapping("/{fileId}")
  public ResponseEntity<Resource> downloadFile(
      @PathVariable Long fileId,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    FileDownloadDTO downloadDTO = fileService.downloadFile(fileId, member);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(downloadDTO.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + downloadDTO.getOriginalName() + "\"")
        .body(downloadDTO.getResource());
  }

  @Operation(summary = "파일 삭제")
  @DeleteMapping("/{fileId}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable Long fileId,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    fileService.deleteFile(fileId, member);
    return ResponseEntity.noContent().build();
  }
}
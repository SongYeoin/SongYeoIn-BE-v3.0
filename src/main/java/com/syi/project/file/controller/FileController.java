package com.syi.project.file.controller;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.file.dto.FileUpdateDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 API")
public class FileController {
  private final FileService fileService;
  private final MemberRepository memberRepository;
  private final S3Uploader s3Uploader;  // S3Uploader 추가

  @Operation(summary = "단일 파일 업로드")
  @PostMapping("/upload")
  public ResponseEntity<FileResponseDTO> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("dirName") String dirName,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    File uploadedFile = fileService.uploadFile(file, dirName, member);
    FileResponseDTO response = FileResponseDTO.from(uploadedFile, s3Uploader);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "다중 파일 업로드")
  @PostMapping("/upload/multiple")
  public ResponseEntity<List<FileResponseDTO>> uploadMultipleFiles(
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam("dirName") String dirName,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    List<File> uploadedFiles = fileService.uploadFiles(files, dirName, member);
    List<FileResponseDTO> response = uploadedFiles.stream()
        .map(file -> FileResponseDTO.from(file, s3Uploader))
        .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "단일 파일 수정")
  @PutMapping("/{fileId}")
  public ResponseEntity<FileResponseDTO> updateFile(
      @PathVariable Long fileId,
      @RequestParam(value = "file", required = false) MultipartFile newFile,
      @RequestParam("dirName") String dirName,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    File updatedFile = fileService.updateFile(fileId, newFile, dirName, member);
    FileResponseDTO response = FileResponseDTO.from(updatedFile, s3Uploader);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "다중 파일 수정")
  @PutMapping("/update/multiple")
  public ResponseEntity<List<FileResponseDTO>> updateMultipleFiles(
      @ModelAttribute FileUpdateDTO updateDTO,
      @RequestParam("dirName") String dirName,
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    List<File> updatedFiles = fileService.updateFiles(updateDTO, dirName, member);
    List<FileResponseDTO> response = updatedFiles.stream()
        .map(file -> FileResponseDTO.from(file, s3Uploader))
        .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

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
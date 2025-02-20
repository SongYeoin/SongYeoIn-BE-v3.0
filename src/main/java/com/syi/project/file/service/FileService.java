package com.syi.project.file.service;

import com.amazonaws.util.IOUtils;
import com.syi.project.auth.entity.Member;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.dto.FileUpdateDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.enums.FileStatus;
import com.syi.project.file.repository.FileRepository;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 관련 비즈니스 로직을 처리하는 서비스
 * - S3 저장소와 연동하여 실제 파일 업로드/다운로드/삭제 처리
 * - 파일 메타데이터 관리
 * - 다중 파일 처리 시 트랜잭션 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
  private final FileRepository fileRepository;
  private final S3Uploader s3Uploader;

  // 다중 파일 업로드
  @Transactional
  public List<File> uploadFiles(List<MultipartFile> multipartFiles, String dirName, Member uploader, LocalDate targetDate) {
    List<File> uploadedFiles = new ArrayList<>();

    for (MultipartFile file : multipartFiles) {
      try {
        File uploadedFile = uploadFile(file, dirName, uploader, targetDate);  // targetDate 전달
        uploadedFiles.add(uploadedFile);
      } catch (Exception e) {
        log.error("파일 업로드 실패: {}", file.getOriginalFilename());
        // 이미 업로드된 파일들 롤백
        uploadedFiles.forEach(uploaded -> {
          try {
            deleteFile(uploaded.getId(), uploader);
          } catch (Exception ex) {
            log.error("파일 롤백 실패: {}", uploaded.getOriginalName());
          }
        });
        throw new RuntimeException("다중 파일 업로드 중 오류가 발생했습니다.", e);
      }
    }
    return uploadedFiles;
  }

  // 다중 파일 수정
  @Transactional
  public List<File> updateFiles(FileUpdateDTO updateDTO, String dirName, Member modifier, LocalDate originalPostDate) {
    List<File> updatedFiles = new ArrayList<>();

    // 1. 삭제 요청된 파일들 처리
    if (updateDTO.getDeleteFileIds() != null && !updateDTO.getDeleteFileIds().isEmpty()) {
      for (Long fileId : updateDTO.getDeleteFileIds()) {
        deleteFile(fileId, modifier);
      }
    }

    // 2. 새로운 파일들 추가
    if (updateDTO.getNewFiles() != null && !updateDTO.getNewFiles().isEmpty()) {
      for (MultipartFile newFile : updateDTO.getNewFiles()) {
        try {
          File uploadedFile = uploadFile(newFile, dirName, modifier, originalPostDate);  // this.uploadFile 또는 uploadFile로 호출
          updatedFiles.add(uploadedFile);  // save는 uploadFile 내부에서 이미 하고 있음
        } catch (Exception e) {
          log.error("새 파일 업로드 실패: {}", newFile.getOriginalFilename());
          throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
      }
    }

    return updatedFiles;
  }

  // 단일 파일 업로드
  @Transactional
  public File uploadFile(MultipartFile multipartFile, String dirName, Member uploader, LocalDate targetDate) {
    try {
      // 1. S3에 파일 업로드하고 저장 경로 받기
      String fullPath = s3Uploader.uploadFile(multipartFile, dirName, targetDate);

      // 2. DB에 메타데이터 저장
      File file = File.builder()
          .originalName(multipartFile.getOriginalFilename())
          .objectKey(fullPath)        // 전체 경로를 objectKey로 저장
          .path(fullPath)             // 전체 경로를 path로 저장
          .size(multipartFile.getSize())
          .mimeType(multipartFile.getContentType())
          .uploadedBy(uploader)
          .status(FileStatus.ACTIVE)
          .build();

      return fileRepository.save(file);
    } catch (IOException e) {
      log.error("파일 업로드 실패: {}", e.getMessage());
      throw new RuntimeException("파일 업로드에 실패했습니다.", e);
    }
  }

  // 단일 파일 수정
  @Transactional
  public File updateFile(Long fileId, MultipartFile newFile, String dirName, Member modifier, LocalDate originalPostDate) {
    File existingFile = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

    // 새 파일이 없으면 기존 파일 유지
    if (newFile == null || newFile.isEmpty()) {
      return existingFile;
    }

    try {
      // 1. 새 파일 업로드 시도 - originalPostDate 사용
      String newFileUrl = s3Uploader.uploadFile(newFile, dirName, originalPostDate);

      // 2. 기존 파일 삭제
      s3Uploader.deleteFile(existingFile.getPath());

      // 3. 파일 메타데이터 업데이트
      String newObjectKey = extractObjectKeyFromUrl(newFileUrl);
      existingFile.updateFile(
          newFile.getOriginalFilename(),
          newObjectKey,
          dirName + "/" + newObjectKey,
          newFile.getSize(),
          newFile.getContentType(),
          modifier
      );

      return fileRepository.save(existingFile);
    } catch (Exception e) {
      log.error("파일 수정 실패: {}", e.getMessage());
      throw new RuntimeException("파일 수정에 실패했습니다.", e);
    }
  }

  // S3에서 파일을 삭제하고, 데이터베이스에서 해당 파일의 메타데이터를 삭제하는 메서드
  @Transactional
  public void deleteFile(Long fileId, Member member) {
    // 1. DB에서 파일 정보 조회
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

    // 2. S3에서 실제 파일 삭제
    try {
      s3Uploader.deleteFile(file.getPath());
      file.delete(member); // Member 객체 전달, status를 DELETED로 변경
    } catch (Exception e) {
      log.error("파일 삭제 실패: {}", e.getMessage());
      throw new RuntimeException("파일 삭제에 실패했습니다.", e);
    }
  }

  // 파일 다운로드
  public FileDownloadDTO downloadFile(Long fileId, Member member) {
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

    try {
      Resource resource = new InputStreamResource(s3Uploader.downloadFile(file.getPath()));

      return FileDownloadDTO.builder()
          .originalName(file.getOriginalName())
          .contentType(file.getMimeType())
          .resource(resource)
          .build();
    } catch (Exception e) {
      log.error("파일 다운로드 실패: {}", e.getMessage());
      throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
    }
  }

  // 일괄 다운로드 zip파일
  @Transactional(readOnly = true)
  public Resource downloadFilesAsZip(List<File> files, String zipFileName) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(baos);

      for (File file : files) {
        InputStream inputStream = s3Uploader.downloadFile(file.getPath());
        ZipEntry entry = new ZipEntry(file.getOriginalName());
        zos.putNextEntry(entry);
        IOUtils.copy(inputStream, zos);
        zos.closeEntry();
        inputStream.close();
      }

      zos.close();
      return new ByteArrayResource(baos.toByteArray());
    } catch (IOException e) {
      log.error("파일 압축 실패: {}", e.getMessage());
      throw new RuntimeException("파일 압축에 실패했습니다.", e);
    }
  }

  //추가
  public ResponseEntity<Resource> getDownloadResponseEntity(FileDownloadDTO downloadDTO) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(downloadDTO.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
            URLEncoder.encode(downloadDTO.getOriginalName(), StandardCharsets.UTF_8))
        .body(downloadDTO.getResource());
  }

  // 파일 URL에서 S3 객체 키를 추출하는 헬퍼 메서드
  private String extractObjectKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
  }
}
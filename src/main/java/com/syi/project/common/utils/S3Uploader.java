package com.syi.project.common.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.syi.project.file.dto.FileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  // 파일 업로드 (단일/다중 처리 통합)
  @Operation(summary = "파일 업로드 (단일/다중)")
  @PostMapping("/upload")
  public String uploadFile(MultipartFile file, String dirName) throws IOException {
    String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
    String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String fileName = createFileName(file.getOriginalFilename());

    // dirName/memberId/dateFolder/fileName 형태로 경로 구성
    String fullPath = String.format("%s/%s/%s/%s", dirName, memberId, dateFolder, fileName);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());

    amazonS3Client.putObject(
        new PutObjectRequest(bucket, fullPath, file.getInputStream(), metadata)
    );

    log.info("파일 업로드 성공 경로: {}", fullPath);
    return fullPath;
  }

  // 파일 수정
  public String updateFile(MultipartFile newFile, String dirName, String oldFileUrl) throws IOException {
    try {
      // 1. 기존 파일 삭제
      if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
        deleteFile(oldFileUrl);
        log.info("기존 파일 삭제 완료.");
      }

      // 2. 새 파일 업로드
      String newFileUrl = uploadFile(newFile, dirName);
      log.info("새 파일 업로드 완료: {}", newFileUrl);

      return newFileUrl;
    } catch (IOException e) {
      log.error("파일 수정 중 에러가 발생했습니다.", e);
      throw new RuntimeException("파일 수정 중 에러가 발생했습니다.", e);
    }
  }

  // 파일 삭제
  public void deleteFile(String fileUrl) {
    try {
      if (fileUrl == null || fileUrl.isEmpty()) {
        throw new IllegalArgumentException("파일 URL이 null이거나 비어있습니다.");
      }

      String fileName = fileUrl;  // 이미 Object Key일 경우, 그대로 사용

      amazonS3Client.deleteObject(bucket, fileName);
      log.info("파일 삭제 완료: {}", fileName);

    } catch (IllegalArgumentException e) {
      log.error("파일 삭제 중 유효성 검증 실패: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("파일 삭제 중 에러가 발생했습니다: {}", e.getMessage());
      throw new RuntimeException("파일 삭제 중 에러가 발생했습니다.", e);
    }
  }

  // 파일 다운로드
  public InputStream downloadFile(String path) {
    S3Object s3Object = amazonS3Client.getObject(bucket, path);
    return s3Object.getObjectContent();
  }

  // UUID(파일명 중복방지)로 파일명 생성
  private String createFileName(String originalFileName) {
    return UUID.randomUUID().toString() + getFileExtension(originalFileName);
  }

  // 파일 확장자 추출
  private String getFileExtension(String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf("."));
    } catch (StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("잘못된 형식의 파일입니다.");
    }
  }

  // S3 URL 반환
  public String getUrl(String path) {
    return amazonS3Client.getUrl(bucket, path).toString();
  }
}
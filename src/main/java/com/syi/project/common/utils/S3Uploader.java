package com.syi.project.common.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

  // 단일 파일 업로드
  public String uploadFile(MultipartFile file, String dirName) throws IOException {
    String fileName = createFileName(file.getOriginalFilename());
    String fullPath = dirName + "/" + fileName;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());

    amazonS3Client.putObject(
        new PutObjectRequest(bucket, fullPath, file.getInputStream(), metadata)
    );
    return amazonS3Client.getUrl(bucket, fullPath).toString();
  }

  // 다중 파일 업로드
  public List<String> uploadFiles(List<MultipartFile> files, String dirName) {
    List<String> uploadUrls = new ArrayList<>();

    files.forEach(file -> {
      try {
        String uploadUrl = uploadFile(file, dirName);
        uploadUrls.add(uploadUrl);
      } catch (IOException e) {
        throw new RuntimeException("파일 업로드 중 에러가 발생했습니다.", e);
      }
    });

    return uploadUrls;
  }

  // 파일 삭제 - 파일 URL을 사용해 S3에서 파일을 삭제
  public void deleteFile(String fileUrl) {
    try {
      if (fileUrl == null || fileUrl.isEmpty()) {
        throw new IllegalArgumentException("파일 URL이 null이거나 비어있습니다.");
      }

      String bucketDomain = bucket + ".s3." + region + ".amazonaws.com/";
      int startIndex = fileUrl.indexOf(bucketDomain);

      if (startIndex == -1) {
        throw new IllegalArgumentException("잘못된 S3 URL 형식입니다: " + fileUrl);
      }

      String fileName = fileUrl.substring(startIndex + bucketDomain.length());
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

  // UUID(파일이름 중복방지)로 파일명 생성
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
}
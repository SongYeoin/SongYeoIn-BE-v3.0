package com.syi.project.common.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

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

  // 파일 삭제
  public void deleteFile(String fileUrl) {
    String fileName = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
    amazonS3Client.deleteObject(bucket, fileName);
  }

  // UUID로 파일명 생성
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
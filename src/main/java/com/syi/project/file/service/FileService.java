package com.syi.project.file.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.enums.FileStatus;
import com.syi.project.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
  private final FileRepository fileRepository;
  private final S3Uploader s3Uploader;

  // 파일을 S3에 업로드하고, 업로드된 파일의 메타데이터를 데이터베이스에 저장하는 메서드
  @Transactional
  public File uploadFile(MultipartFile multipartFile, String dirName, Member uploader) {
    try {
      // 1. S3에 파일 업로드
      String fileUrl = s3Uploader.uploadFile(multipartFile, dirName);
      String objectKey = extractObjectKeyFromUrl(fileUrl);

      // 2. DB에 파일 메타데이터 저장
      File file = File.builder()
          .originalName(multipartFile.getOriginalFilename())
          .objectKey(objectKey)
          .path(dirName + "/" + objectKey)
          .size(multipartFile.getSize())
          .mimeType(multipartFile.getContentType())
          .uploadedBy(uploader)
          .status(FileStatus.ACTIVE)
          .build();

      return fileRepository.save(file); // 메타데이터 저장 후 리턴
    } catch (IOException e) {
      log.error("파일 업로드 실패: {}", e.getMessage());
      throw new RuntimeException("파일 업로드에 실패했습니다.", e);
    }
  }

  // S3에서 파일을 삭제하고, 데이터베이스에서 해당 파일의 메타데이터를 삭제하는 메서드
  @Transactional
  public void deleteFile(Long fileId, Member member) {
    // 1. DB에서 파일 정보 조회
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

    // 2. S3에서 실제 파일 삭제
    s3Uploader.deleteFile(file.getPath());
    file.delete(member);  // Member 객체 전달, status를 DELETED로 변경
  }

  // 파일 다운로드
  public FileDownloadDTO downloadFile(Long fileId, Member member) {
    // 1. DB에서 파일 정보 조회
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

    // 2. S3에서 파일 스트림 가져오기
    // Resource 타입 변환 처리
    Resource resource = new InputStreamResource(s3Uploader.downloadFile(file.getPath()));

    return FileDownloadDTO.builder()
        .originalName(file.getOriginalName())
        .contentType(file.getMimeType())
        .resource(resource)
        .build();
  }

  // 파일 URL에서 S3 객체 키를 추출하는 헬퍼 메서드
  private String extractObjectKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1); // URL에서 객체 키만 추출
  }
}
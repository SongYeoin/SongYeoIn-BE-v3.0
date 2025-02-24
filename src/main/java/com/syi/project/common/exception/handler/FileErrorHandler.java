package com.syi.project.common.exception.handler;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.syi.project.file.entity.File;
import com.syi.project.file.enums.FileStatus;
import com.syi.project.file.repository.FileRepository;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileErrorHandler {
  private final FileRepository fileRepository;

  public File validateFileExists(Long fileId) {
    return fileRepository.findById(fileId)
        .orElseThrow(() -> {
          log.error("파일을 찾을 수 없음 - fileId: {}", fileId);
          return new InvalidRequestException(ErrorCode.FILE_NOT_FOUND);
        });
  }

  public void validateFileStatus(File file) {
    if (file.getStatus() != FileStatus.ACTIVE) {
      log.error("파일이 유효하지 않은 상태 - fileId: {}, status: {}", file.getId(), file.getStatus());
      throw new InvalidRequestException(ErrorCode.FILE_INVALID_STATE);
    }
  }

  public void handleS3DownloadError(AmazonS3Exception e, Long fileId, String path) {
    if ("NoSuchKey".equals(e.getErrorCode())) {
      log.error("S3에서 파일을 찾을 수 없음 - fileId: {}, path: {}", fileId, path);
      throw new InvalidRequestException(ErrorCode.FILE_NOT_IN_STORAGE);
    }
    log.error("S3 파일 다운로드 실패 - fileId: {}, error: {}", fileId, e.getMessage());
    throw new InvalidRequestException(ErrorCode.FILE_DOWNLOAD_FAILED);
  }
}
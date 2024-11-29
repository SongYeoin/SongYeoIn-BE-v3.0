package com.syi.project.file.dto;

import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.entity.File;
import lombok.Getter;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

// 파일 업로드/조회 결과를 클라이언트에 전달
@Getter
public class FileResponseDTO {
  private Long id;
  private String originalName;
  private String url;
  private String mimeType;
  private String size;
  private LocalDateTime uploadedAt;
  private String uploadedBy;

  private FileResponseDTO(Long id, String originalName, String url, String mimeType,
      String size, LocalDateTime uploadedAt, String uploadedBy) {
    this.id = id;
    this.originalName = originalName;
    this.url = url;
    this.mimeType = mimeType;
    this.size = size;
    this.uploadedAt = uploadedAt;
    this.uploadedBy = uploadedBy;
  }

  public static FileResponseDTO from(File file, S3Uploader s3Uploader) {
    return new FileResponseDTO(
        file.getId(),
        file.getOriginalName(),
        s3Uploader.getUrl(file.getPath()),
        file.getMimeType(),
        formatFileSize(file.getSize()),
        file.getCreatedAt(),
        file.getUploadedBy().getName()
    );
  }

  private static String formatFileSize(long size) {
    if (size <= 0) return "0";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
    return new DecimalFormat("#,##0.#")
        .format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
}
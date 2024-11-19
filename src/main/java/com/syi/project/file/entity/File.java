package com.syi.project.file.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.entity.BaseTimeEntity;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.enums.FileStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files",
    indexes = {
        @Index(columnList = "objectKey", name = "uk_files_objectKey", unique = true),
        @Index(columnList = "uploadedBy", name = "idx_files_uploadedBy"),
        @Index(columnList = "mimeType", name = "idx_files_mimeType"),
        @Index(columnList = "status", name = "idx_files_status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class File extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String originalName; // 원본 파일명

  @Column(nullable = false)
  private String objectKey; // S3에 저장된 파일 키

  @Column(nullable = false)
  private String path; // S3 저장 경로 (dirName + objectKey)

  @Column(nullable = false)
  private Long size; // 파일 크기

  @Column(nullable = false)
  private String mimeType; // 파일 타입

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  private Member uploadedBy; // 업로드한 사용자

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FileStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by")
  private Member deletedBy;

  private LocalDateTime deletedAt;

  public void delete(Member deletedBy) {
    this.status = FileStatus.DELETED; // 파일 상태
    this.deletedBy = deletedBy; // 누가 삭제했는지
    this.deletedAt = LocalDateTime.now(); // 언제 삭제됐는지
  }

  public void updateFile(
      String originalName,
      String objectKey,
      String path,
      Long size,
      String mimeType,
      Member modifier
  ) {
    this.originalName = originalName;
    this.objectKey = objectKey;
    this.path = path;
    this.size = size;
    this.mimeType = mimeType;
  }

  // S3 URL을 반환하는 메서드
  public String getUrl(S3Uploader s3Uploader) {
    return s3Uploader.getUrl(this.path);  // S3Uploader를 사용하여 URL 반환
  }
}
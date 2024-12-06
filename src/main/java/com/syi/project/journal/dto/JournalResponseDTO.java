package com.syi.project.journal.dto;

import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.journal.entity.Journal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.Getter;

@Getter
public class JournalResponseDTO {
  private Long id;
  private String memberName;
  private String courseName;
  private String title;
  private String content;
  private FileResponseDTO file;
  private LocalDateTime createdAt;
  private LocalDate educationDate;

  private JournalResponseDTO(Long id, String memberName, String courseName,
      String title, String content, FileResponseDTO file,
      LocalDateTime createdAt, LocalDate educationDate) {
    this.id = id;
    this.memberName = memberName;
    this.courseName = courseName;
    this.title = title;
    this.content = content;
    this.file = file;
    this.createdAt = createdAt;
    this.educationDate = educationDate;
  }

  // from 메서드 유지 (기존 코드와의 일관성)
  public static JournalResponseDTO from(Journal journal, S3Uploader s3Uploader) {
    return new JournalResponseDTO(
        journal.getId(),
        journal.getMember().getName(),
        journal.getCourse().getName(),
        journal.getTitle(),
        journal.getContent(),
        Optional.ofNullable(journal.getJournalFile())
            .map(file -> FileResponseDTO.from(file.getFile(), s3Uploader))
            .orElse(null),
        journal.getCreatedAt(),
        journal.getEducationDate()
    );
  }

  // of 메서드 추가 (대체 팩토리 메서드)
  public static JournalResponseDTO of(Journal journal, S3Uploader s3Uploader) {
    return from(journal, s3Uploader);
  }
}
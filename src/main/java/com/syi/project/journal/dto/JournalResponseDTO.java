package com.syi.project.journal.dto;

import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.journal.entity.Journal;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class JournalResponseDTO {
  private Long id;
  private String memberName;
  private String courseName;
  private String title;
  private String content;
  private FileResponseDTO file;  // List에서 단일로 변경
  private LocalDateTime createdAt;

  private JournalResponseDTO(Long id, String memberName, String courseName,
      String title, String content, FileResponseDTO file,
      LocalDateTime createdAt) {
    this.id = id;
    this.memberName = memberName;
    this.courseName = courseName;
    this.title = title;
    this.content = content;
    this.file = file;
    this.createdAt = createdAt;
  }

  public static JournalResponseDTO from(Journal journal, S3Uploader s3Uploader) {
    return new JournalResponseDTO(
        journal.getId(),
        journal.getMember().getName(),
        journal.getCourse().getName(),
        journal.getTitle(),
        journal.getContent(),
        journal.getJournalFile() != null
            ? FileResponseDTO.from(journal.getJournalFile().getFile(), s3Uploader)
            : null,
        journal.getCreatedAt()
    );
  }
}
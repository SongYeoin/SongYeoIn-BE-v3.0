package com.syi.project.journal.dto;

import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.journal.entity.Journal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class JournalResponseDTO {
  private Long id;
  private String memberName;
  private String courseName;
  private String title;
  private String content;
  private List<FileResponseDTO> files;
  private LocalDateTime createdAt;

  private JournalResponseDTO(Long id, String memberName, String courseName,
      String title, String content, List<FileResponseDTO> files,
      LocalDateTime createdAt) {
    this.id = id;
    this.memberName = memberName;
    this.courseName = courseName;
    this.title = title;
    this.content = content;
    this.files = files;
    this.createdAt = createdAt;
  }

  public static JournalResponseDTO from(Journal journal, S3Uploader s3Uploader) {
    return new JournalResponseDTO(
        journal.getId(),
        journal.getMember().getName(),
        journal.getCourse().getName(),
        journal.getTitle(),
        journal.getContent(),
        journal.getJournalFiles().stream()
            .map(jf -> FileResponseDTO.from(jf.getFile(), s3Uploader))
            .collect(Collectors.toList()),
        journal.getCreatedAt()
    );
  }
}
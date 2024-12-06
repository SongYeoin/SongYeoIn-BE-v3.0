package com.syi.project.journal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public class JournalRequestDTO {

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Create {
    @NotNull(message = "강좌 ID는 필수입니다")
    private Long courseId;
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    private String content;
    @NotNull(message = "교육일지 파일을 선택해주세요")
    private MultipartFile file;
    @NotNull(message = "교육일자는 필수입니다")
    private LocalDate educationDate;

    @Builder
    public Create(Long courseId, String title, String content,
        MultipartFile file, LocalDate educationDate) {
      this.courseId = courseId;
      this.title = title;
      this.content = content;
      this.file = file;
      this.educationDate = educationDate;
    }
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Update {
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    private String content;
    private MultipartFile file;
    @NotNull(message = "교육일자는 필수입니다")
    private LocalDate educationDate;

    @Builder
    public Update(String title, String content,
        MultipartFile file, LocalDate educationDate) {
      this.title = title;
      this.content = content;
      this.file = file;
      this.educationDate = educationDate;
    }
  }
}
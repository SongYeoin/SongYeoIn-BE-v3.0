package com.syi.project.journal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// 일지 작성 시 필요한 정보를 담고 있음. 일지 작성 요청을 처리함
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalRequestDTO {

  @NotNull(message = "강좌 ID는 필수입니다")
  private Long courseId;
  @NotBlank(message = "제목은 필수입니다")
  private String title;
  private String content;
  @NotNull(message = "교육일지 파일을 선택해주세요")
  private MultipartFile file;  // 한글 파일 업로드용

  @Builder
  public JournalRequestDTO(Long courseId, String title, String content, MultipartFile file) {
    this.courseId = courseId;
    this.title = title;
    this.content = content;
    this.file = file;
  }
}

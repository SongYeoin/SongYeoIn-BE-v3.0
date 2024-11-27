package com.syi.project.notice.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import com.syi.project.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@ToString
@Schema(description = "공지사항 요쳥 DTO")
public class NoticeRequestDTO {

  @NotBlank(message = "제목은 필수입니다.")
  @Schema(description = "공지사항 제목", example = "공지사항 제목")
  private final String title;

  @NotBlank(message = "내용은 필수입니다.")
  @Schema(description = "공지사항 내용", example = "공지사항 내용")
  private final String content;

  @Schema(description = "강의 ID(null일 경우 전체 공지)", example = "1")
  private final Long courseId;

  @Schema(description = "전체 공지 여부", example = "false")
  private final boolean isGlobal;

  @Builder
  public NoticeRequestDTO(String title, String content, Long courseId, boolean isGlobal) {
    this.title = title;
    this.content = content;
    this.courseId = courseId;
    this.isGlobal = isGlobal;
  }

  public Notice toEntity(Member member, Course course) {
    return Notice.builder()
        .title(this.title)
        .content(this.content)
        .member(member)
        .course(course)
        .isGlobal(this.isGlobal)
        .build();
  }

}

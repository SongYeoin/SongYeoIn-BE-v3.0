package com.syi.project.notice.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import com.syi.project.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

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

  @Schema(description = "교육과정 ID", example = "1")
  private final Long courseId;

  @Schema(description = "상단고정 여부", example = "false")
  private final boolean isPinned;

  @Builder
  public NoticeRequestDTO(String title, String content, Long courseId, boolean isPinned) {
    this.title = title;
    this.content = content;
    this.courseId = courseId;
    this.isPinned = isPinned;
  }

  public Notice toEntity(Member member, Course course) {
    return Notice.builder()
        .title(this.title)
        .content(this.content)
        .member(member)
        .course(course)
        .isPinned(this.isPinned)
        .build();
  }

}

package com.syi.project.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.notice.entity.Notice;
import com.syi.project.notice.entity.NoticeFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "공지사항 응답 DTO")
public class NoticeResponseDTO {

  @Schema(description = "공지사항 ID", example = "1")
  private final Long id;

  @Schema(description = "교육과정 ID", example = "1")
  private final Long courseId;

  @Schema(description = "공지사항 제목", example = "공지사항 제목")
  private final String title;

  @Schema(description = "공지사항 내용", example = "공지사항 내용")
  private final String content;

  @Schema(description = "작성자 이름", example = "홍길동")
  private final String memberName;

  @Schema(description = "공지사항 조회수", example = "123")
  private final Long viewCount;

  @Schema(description = "상단고정 여부", example = "false")
  @JsonProperty("isPinned")
  private final boolean isPinned;

  @Schema(description = "첨부 파일 목록")
  private final List<FileResponseDTO> files;

  @Schema(description = "등록일", example = "2024-11-21")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private final LocalDateTime regDate;

  @Schema(description = "수정일", example = "2024-11-21")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private final LocalDateTime modifyDate;

  @Builder
  public NoticeResponseDTO(Long id, Long courseId, String title, String content,
      String memberName, Long viewCount, boolean isPinned, List<FileResponseDTO> files,
      LocalDateTime regDate, LocalDateTime modifyDate) {
    this.id = id;
    this.courseId = courseId;
    this.title = title;
    this.content = content;
    this.memberName = memberName;
    this.viewCount = viewCount;
    this.isPinned = isPinned;
    this.files = files;
    this.regDate = regDate;
    this.modifyDate = modifyDate;
  }

  public static NoticeResponseDTO fromEntity(Notice notice, S3Uploader s3Uploader) {
    return NoticeResponseDTO.builder()
        .id(notice.getId())
        .courseId(notice.getCourse().getId())
        .title(notice.getTitle())
        .content(notice.getContent())
        .memberName(notice.getMember().getName())
        .viewCount(notice.getViewCount())
        .isPinned(notice.isPinned())
        .files(notice.getFiles().stream()
            .map(NoticeFile::getFile)
            .map(file -> FileResponseDTO.from(file, s3Uploader))
            .collect(Collectors.toList()))
        .regDate(notice.getRegDate())
        .modifyDate(notice.getModifyDate())
        .build();
  }

}

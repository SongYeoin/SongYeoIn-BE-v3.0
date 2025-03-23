package com.syi.project.support.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.syi.project.support.entity.DeveloperResponse;
import com.syi.project.support.entity.Support;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "고객센터 문의 응답 DTO")
public class SupportResponseDTO {

  @Schema(description = "문의 ID", example = "1")
  private final Long id;

  @Schema(description = "문의 제목", example = "시스템 오류 발생")
  private final String title;

  @Schema(description = "문의 내용", example = "로그인 후 메인 페이지 접속 시 오류가 발생합니다.")
  private final String content;

  @Schema(description = "작성자 이름", example = "홍길동")
  private final String memberName;

  @Schema(description = "확인 여부", example = "false")
  @JsonProperty("isConfirmed")
  private final boolean isConfirmed;

  @Schema(description = "등록일", example = "2024-03-02")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private final LocalDateTime regDate;

  @Schema(description = "수정일", example = "2024-03-02 14:30:00")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private final LocalDateTime modifyDate;

  @Schema(description = "개발팀 응답", nullable = true)
  private final DeveloperResponseDTO developerResponse;

  @Builder
  public SupportResponseDTO(Long id, String title, String content, String memberName,
      boolean isConfirmed, LocalDateTime regDate, LocalDateTime modifyDate,
      DeveloperResponseDTO developerResponse) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.memberName = memberName;
    this.isConfirmed = isConfirmed;
    this.regDate = regDate;
    this.modifyDate = modifyDate;
    this.developerResponse = developerResponse;
  }

  public static SupportResponseDTO fromEntity(Support support) {
    return SupportResponseDTO.builder()
        .id(support.getId())
        .title(support.getTitle())
        .content(support.getContent())
        .memberName(support.getMember().getName())
        .isConfirmed(support.isConfirmed())
        .regDate(support.getRegDate())
        .modifyDate(support.getModifyDate())
        .build();
  }

  public static SupportResponseDTO fromEntity(Support support, DeveloperResponse developerResponse) {
    return SupportResponseDTO.builder()
        .id(support.getId())
        .title(support.getTitle())
        .content(support.getContent())
        .memberName(support.getMember().getName())
        .isConfirmed(support.isConfirmed())
        .regDate(support.getRegDate())
        .modifyDate(support.getModifyDate())
        .developerResponse(developerResponse != null ? DeveloperResponseDTO.fromEntity(developerResponse) : null)
        .build();
  }
}
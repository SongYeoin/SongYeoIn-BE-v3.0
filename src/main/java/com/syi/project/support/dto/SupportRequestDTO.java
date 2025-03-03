package com.syi.project.support.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.support.entity.Support;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "고객센터 문의 요청 DTO")
public class SupportRequestDTO {

  @NotBlank(message = "제목은 필수입니다.")
  @Schema(description = "문의 제목", example = "시스템 오류 발생")
  private final String title;

  @NotBlank(message = "내용은 필수입니다.")
  @Schema(description = "문의 내용", example = "로그인 후 메인 페이지 접속 시 오류가 발생합니다.")
  private final String content;

  @Builder
  public SupportRequestDTO(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public Support toEntity(Member member) {
    return Support.builder()
        .title(this.title)
        .content(this.content)
        .member(member)
        .build();
  }
}
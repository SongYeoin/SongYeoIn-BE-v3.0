package com.syi.project.support.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syi.project.support.entity.DeveloperResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "개발팀 응답 DTO")
public class DeveloperResponseDTO {

  @Schema(description = "응답 ID", example = "1")
  private Long id;

  @Schema(description = "문의 ID", example = "123")
  private Long supportId;

  @Schema(description = "응답 내용", example = "확인 결과 버그가 확인되었으며, 다음 업데이트에서 수정될 예정입니다.")
  private String responseContent;

  @Schema(description = "개발자 ID", example = "dev123")
  private String developerId;

  @Schema(description = "개발자 이름", example = "홍길동")
  private String developerName;

  @Schema(description = "응답 일시", example = "2024-03-07 14:30:00")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime regDate;

  public static DeveloperResponseDTO fromEntity(DeveloperResponse response) {
    return DeveloperResponseDTO.builder()
        .id(response.getId())
        .supportId(response.getSupportId())
        .responseContent(response.getResponseContent())
        .developerId(response.getDeveloperId())
        .developerName(response.getDeveloperName())
        .regDate(response.getCreatedAt()) // BaseTimeEntity에서 정의된 필드명으로 변경
        .build();
  }
}
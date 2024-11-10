package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "중복 체크 응답 DTO")
public class DuplicateCheckDTO {

  @Schema(description = "중복 여부", example = "true")
  private final boolean isAvailable;

  @Schema(description = "중복 여부", example = "true")
  private final String message;

  @Builder
  public DuplicateCheckDTO(boolean isAvailable, String message) {
    this.isAvailable = isAvailable;
    this.message = message;
  }
}

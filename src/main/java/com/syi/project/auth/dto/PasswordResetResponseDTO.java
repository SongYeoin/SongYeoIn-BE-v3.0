package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description="비밀번호 초기화 응답 DTO")
public class PasswordResetResponseDTO {

  @Schema(description = "임시 비밀번호", example = "Ab1!kX9pM")
  private final String temporaryPassword;

  @Schema(description = "초기화 시간")
  private final LocalDateTime resetTime;

  @Builder
  public PasswordResetResponseDTO(String temporaryPassword, LocalDateTime resetTime) {
    this.temporaryPassword = temporaryPassword;
    this.resetTime = resetTime;
  }
}

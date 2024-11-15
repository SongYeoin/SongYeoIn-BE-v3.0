package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "로그인 응답 DTO")
public class MemberLoginResponseDTO {

  @Schema(description = "JWT Access Token")
  private final String accessToken;

  @Schema(description = "JWT Refresh Token")
  private final String refreshToken;

  @Builder
  public MemberLoginResponseDTO(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}

package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청 DTO")
public class MemberLoginRequestDTO {

  @NotBlank(message = "아이디는 필수입니다.")
  @Schema(description = "회원 아이디", example = "user123")
  private final String username;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Schema(description = "회원 비밀번호", example = "password123")
  private final String password;

  @Builder
  public MemberLoginRequestDTO(String username, String password) {
    this.username = username;
    this.password = password;
  }
}

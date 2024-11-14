package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Schema(description = "로그인 요청 DTO")
public class MemberLoginRequestDTO {

  @NotBlank(message = "아이디는 필수입니다.")
  @Schema(description = "회원 아이디", example = "user123")
  private final String memberId;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Schema(description = "회원 비밀번호", example = "password123")
  private final String password;

  @Builder
  public MemberLoginRequestDTO(String memberId, String password) {
    this.memberId = memberId;
    this.password = password;
  }
}

package com.syi.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "회원정보 수정 요청 DTO")
public class MemberUpdateRequestDTO {

  @Schema(description = "현재 비밀번호", example = "currentPassword123!")
  private String currentPassword;

  @Schema(description = "비밀번호", example = "password123!")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]{8,16}$",
      message = "비밀번호는 8~16자의 영문자, 숫자, 특수문자(!@#$%^&*)를 포함해야 합니다.")
  private String password;

  @Schema(description = "비밀번호 확인", example = "password123!")
  private String confirmPassword;

  @Schema(description = "이메일 주소", example = "updated@example.com")
  @Email(message = "유효한 이메일 주소를 입력하세요.")
  private String email;
}

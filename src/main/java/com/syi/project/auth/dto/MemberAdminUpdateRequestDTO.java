package com.syi.project.auth.dto;

import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "관리자가 수강생의 정보를 수정하기 위해 사용하는 DTO")
public class MemberAdminUpdateRequestDTO {

  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  @NotBlank(message = "아이디는 필수 입력값입니다.")
  private String username;

  @NotBlank(message = "생년월일은 필수 입력값입니다.")
  private String birthday;

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotNull(message = "역할은 필수 입력값입니다.")
  private Role role;

  @NotNull(message = "승인 상태는 필수 입력값입니다.")
  private CheckStatus checkStatus;

}

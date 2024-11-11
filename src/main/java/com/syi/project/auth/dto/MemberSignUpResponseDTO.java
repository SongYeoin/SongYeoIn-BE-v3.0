package com.syi.project.auth.dto;

import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 응답 DTO")
public class MemberSignUpResponseDTO {

  @Schema(description = "회원 ID", example = "1")
  private final Long id;

  @Schema(description = "회원 아이디", example = "testUser")
  private final String memberId;

  @Schema(description = "이름", example = "홍길동")
  private final String name;

  @Schema(description = "생년월일", example = "19900101")
  private final String birthday;

  @Schema(description = "이메일 주소", example = "test@example.com")
  private final String email;

  @Schema(description = "회원 역할", example = "STUDENT")
  private final Role role;

  @Builder
  public MemberSignUpResponseDTO(Long id, String memberId, String name, String birthday,
      String email, Role role) {
    this.id = id;
    this.memberId = memberId;
    this.name = name;
    this.birthday = birthday;
    this.email = email;
    this.role = role;
  }
}

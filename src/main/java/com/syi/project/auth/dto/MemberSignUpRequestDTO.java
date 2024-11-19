package com.syi.project.auth.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "회원가입 요청 DTO")
public class MemberSignUpRequestDTO {

  @Schema(description = "회원 아이디", example = "testuser")
  @NotBlank(message = "아이디는 필수입니다.")
  @Pattern(regexp = "^[a-z0-9_]{6,12}$", message = "아이디는 6~12자의 영문 소문자, 숫자, 특수기호(_)만 사용 가능합니다.")
  private final String username;

  @Schema(description = "비밀번호", example = "password123!")
  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]{8,16}$",
      message = "비밀번호는 8~16자의 영문자, 숫자, 특수문자(!@#$%^&*)를 포함해야 합니다.")
  private final String password;

  @Schema(description = "비밀번호 확인", example = "password123!")
  @NotBlank(message = "비밀번호 확인은 필수입니다.")
  private final String confirmPassword;

  @Schema(description = "이름", example = "홍길동")
  @NotBlank(message = "이름은 필수입니다.")
  private final String name;

  @Schema(description = "생년월일 (yyyyMMdd 형식)", example = "19900101")
  @NotBlank(message = "생년월일은 필수입니다.")
  private final String birthday;

  @Schema(description = "이메일 주소", example = "test@example.com")
  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "유효한 이메일 주소를 입력하세요.")
  private final String email;

  @Schema(description = "회원 역할 (STUDENT 또는 ADMIN)", example = "STUDENT")
  @NotNull(message = "역할은 필수입니다.")
  private final Role role;

  @Builder
  public MemberSignUpRequestDTO(String username, String password, String confirmPassword,
      String name,
      String birthday, String email, Role role) {
    this.username = username;
    this.password = password;
    this.confirmPassword = confirmPassword;
    this.name = name;
    this.birthday = birthday;
    this.email = email;
    this.role = role;
  }

  public Member toEntity(String encodedPassword) {
    return new Member(
        this.username,
        encodedPassword,
        this.name,
        this.birthday,
        this.email,
        this.role
    );
  }
}

package com.syi.project.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "회원의 목록 조회와 상세보기에서 사용하는 DTO")
public class MemberDTO {

  @Schema(description = "회원 ID (기본키)", example = "1")
  private final Long id;

  @Schema(description = "회원 아이디 (로그인 ID)", example = "user1234")
  private final String username;

  @Schema(description = "회원 이름", example = "홍길동")
  private final String name;

  @Schema(description = "회원 생년월일", example = "1995.01.01")
  private final String birthday;

  @Schema(description = "회원 이메일 주소", example = "user@example.com")
  private final String email;

  @Schema(description = "회원 가입일", example = "2024.11.20")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private final LocalDateTime enrollDate;

  @Schema(description = "회원 승인 상태", example = "Y", allowableValues = {"Y", "N", "W"})
  private final CheckStatus checkStatus;

  @Schema(description = "회원 역할", example = "STUDENT", allowableValues = {"STUDENT", "ADMIN"})
  private final Role role;

  @Schema(description = "회원 프로필 이미지 URL", example = "https://example.com/profiles/user1234.jpg")
  private final String profileUrl;

  public static MemberDTO fromEntity(Member member) {
    return MemberDTO.builder()
        .id(member.getId())
        .username(member.getUsername())
        .name(member.getName())
        .birthday(member.getBirthday())
        .email(member.getEmail())
        .enrollDate(member.getEnrollDate())
        .checkStatus(member.getCheckStatus())
        .role(member.getRole())
        .profileUrl(member.getProfileUrl())
        .build();
  }

}

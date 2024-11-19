package com.syi.project.auth.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDTO {

  private final Long id;
  private final String username;
  private final String name;
  private final String birthday;
  private final String email;
  private final LocalDate enrollDate;
  private final CheckStatus checkStatus;
  private final Role role;
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

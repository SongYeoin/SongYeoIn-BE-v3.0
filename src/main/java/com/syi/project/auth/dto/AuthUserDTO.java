package com.syi.project.auth.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(description = "인증과 보안 관련 작업에 필요한 최소한의 사용자 정보 DTO")
public class AuthUserDTO {

  @Schema(description = "사용자 기본키", example = "1")
  private final Long id;

  @Schema(description = "사용자 아이디", example = "user1234")
  private final String username;

  @Schema(description = "사용자 역할", example = "STUDENT")
  private final Role role;

  public AuthUserDTO(Long id, String username, Role role) {
    this.id = id;
    this.username = username;
    this.role = role;
  }

  // Member 엔티티를 AuthUserDTO 로 변환하는 정적 메서드
  public static AuthUserDTO fromEntity(Member member) {
    log.debug("Member 엔티티를 AuthUserDTO 로 변환 - ID(기본키): {}, username: {}, Role: {}",
        member.getId(), member.getUsername(), member.getRole());
    return new AuthUserDTO(
        member.getId(),
        member.getUsername(),
        member.getRole()
    );
  }
}

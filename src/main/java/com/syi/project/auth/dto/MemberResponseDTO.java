package com.syi.project.auth.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class MemberResponseDTO {
  private Long id;
  private String memberId;
  private String name;
  private LocalDate birthday;
  private String email;
  private String role;
  private String profileUrl;
}

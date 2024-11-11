package com.syi.project.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
public class MemberLoginRequestDTO {
  private String memberId;
  private String password;
}

package com.syi.project.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestDTO {

  @NotNull(message = "관리자 ID는 필수 항목입니다.")
  private Long adminId;

}

package com.syi.project.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoResponseDTO {
  private LocalDateTime expiryDate;
  private long secondsRemaining;
  private LocalDateTime issuedAt;
  private String tokenId;
}

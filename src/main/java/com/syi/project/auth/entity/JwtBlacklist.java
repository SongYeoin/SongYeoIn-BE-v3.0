package com.syi.project.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtBlacklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false, unique = true)
  private String tokenId; // JWT의 jti 값

  @Column(nullable = false)
  private LocalDateTime expiryDate; // JWT 만료 시간

  @Column(nullable = false)
  private String tokenType; // "ACCESS" 또는 "REFRESH"

  @Column
  private String userAgent; // 사용자 브라우저 정보

  @Column
  private String ipAddress; // 사용자 IP 주소

  @Column
  private String deviceInfo; // 디바이스 정보

  public JwtBlacklist(String tokenId, LocalDateTime expiryDate, String tokenType) {
    this.tokenId = tokenId;
    this.expiryDate = expiryDate;
    this.tokenType = tokenType;
  }

  public JwtBlacklist(String tokenId, LocalDateTime expiryDate, String tokenType,
      String userAgent, String ipAddress, String deviceInfo) {
    this.tokenId = tokenId;
    this.expiryDate = expiryDate;
    this.tokenType = tokenType;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.deviceInfo = deviceInfo;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiryDate);
  }

}

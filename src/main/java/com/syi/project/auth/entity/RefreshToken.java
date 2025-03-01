package com.syi.project.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiryDate;

  @Column
  private String userAgent;

  @Column
  private String ipAddress;

  @Column
  private String deviceInfo;

  public RefreshToken(Long memberId, String token, LocalDateTime expiryDate) {
    this.memberId = memberId;
    this.token = token;
    this.expiryDate = expiryDate;
  }

  public RefreshToken(Long memberId, String token, LocalDateTime expiryDate,
      String userAgent, String ipAddress, String deviceInfo) {
    this.memberId = memberId;
    this.token = token;
    this.expiryDate = expiryDate;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.deviceInfo = deviceInfo;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiryDate);
  }

  public void updateDeviceInfo(String userAgent, String ipAddress, String deviceInfo) {
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.deviceInfo = deviceInfo;
  }
}

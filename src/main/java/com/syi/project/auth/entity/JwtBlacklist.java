package com.syi.project.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class JwtBlacklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false, unique = true)
  private String tokenId; // JWT의 jti 값

  @Column(nullable = false)
  private LocalDateTime expiration; // JWT 만료 시간

  @Column(nullable = false)
  private String tokenType; // "ACCESS" 또는 "REFRESH"

  public JwtBlacklist(String tokenId, LocalDateTime expiration, String tokenType) {
    this.tokenId = tokenId;
    this.expiration = expiration;
    this.tokenType = tokenType;
  }

  public boolean isExpired() {
    return expiration.isBefore(LocalDateTime.now());
  }

}

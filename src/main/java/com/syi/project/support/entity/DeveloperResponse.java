package com.syi.project.support.entity;

import com.syi.project.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "developer_responses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeveloperResponse extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "support_id", nullable = false)
  private Long supportId;

  @Column(name = "response_content", columnDefinition = "TEXT", nullable = false)
  private String responseContent;

  @Column(name = "developer_id")
  private String developerId;

  @Column(name = "developer_name")
  private String developerName;

  @Builder
  public DeveloperResponse(Long supportId, String responseContent, String developerId, String developerName) {
    this.supportId = supportId;
    this.responseContent = responseContent;
    this.developerId = developerId;
    this.developerName = developerName;
  }
}
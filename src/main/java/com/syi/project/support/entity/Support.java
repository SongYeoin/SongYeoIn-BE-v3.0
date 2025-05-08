package com.syi.project.support.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.syi.project.support.enums.SupportStatus;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Support {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  private boolean isConfirmed;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SupportStatus status = SupportStatus.UNCONFIRMED; // 필드 추가

  @Column(nullable = true)
  private Long deletedBy;

  @CreatedDate
  private LocalDateTime regDate;

  @LastModifiedDate
  private LocalDateTime modifyDate;

  @OneToMany(mappedBy = "support", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SupportFile> files = new ArrayList<>();

  @Builder
  public Support(String title, String content, Member member) {
    this.title = title;
    this.content = content;
    this.member = member;
    this.isConfirmed = false;
  }

  public void confirm() {
    this.isConfirmed = true;
  }

  public void unconfirm() {
    this.isConfirmed = false;
  }

  public void markAsDeleted(Long memberId) {
    this.deletedBy = memberId;
  }

  public void addFile(SupportFile supportFile) {
    this.files.add(supportFile);
  }

  public void updateStatus(SupportStatus status) {
    this.status = status;
  }
}
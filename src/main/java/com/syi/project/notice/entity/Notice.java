package com.syi.project.notice.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @CreatedDate
  private LocalDate regDate;

  @LastModifiedDate
  private LocalDate modifyDate;

  @Column(nullable = false)
  private boolean isGlobal;

  @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  private Long viewCount = 0L;

  @OneToMany(mappedBy = "notice", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<NoticeFile> files = new ArrayList<>();

  @Column(nullable = true)
  private Long deletedBy;

  @Builder
  public Notice(String title, String content, Member member, Course course, boolean isGlobal) {
    this.title = title;
    this.content = content;
    this.member = member;
    this.course = course;
    this.isGlobal = isGlobal;
  }

  public void addFile(NoticeFile noticeFile) {
    this.files.add(noticeFile);
  }

  public void incrementViewCount() {
    this.viewCount++;
  }

  public void update(String title, String content, boolean isGlobal) {
    this.title = title;
    this.content = content;
    this.isGlobal = isGlobal;
  }

  public void markAsDeleted(Long memberId) {
    this.deletedBy = memberId;
  }

}
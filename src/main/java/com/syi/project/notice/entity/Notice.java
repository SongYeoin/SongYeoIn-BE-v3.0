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
import java.time.LocalDateTime;
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
  private LocalDateTime noticeRegDate;

  @LastModifiedDate
  private LocalDateTime noticeModifyDate;

  private Long viewCount = 0L;

  private boolean isGlobal;

  private boolean hasFile = false;

  @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<NoticeFile> noticeFiles;

  private Long deletedBy;

  @Builder
  public Notice(String title, String content, Member member, Course course, boolean isGlobal,
      List<NoticeFile> noticeFiles) {
    this.title = title;
    this.content = content;
    this.member = member;
    this.course = course;
    this.isGlobal = isGlobal;
    this.noticeFiles = noticeFiles;
  }

  public void update(String title, String content, List<NoticeFile> noticeFiles, boolean isGlobal) {
    this.title = title;
    this.content = content;
    this.noticeFiles = noticeFiles;
    this.isGlobal = isGlobal;
    this.hasFile = (noticeFiles != null && !noticeFiles.isEmpty()); // 파일 첨부 여부 갱신
  }

  public void markAsDeleted(Long deletedBy) {
    this.deletedBy = deletedBy;
  }

}
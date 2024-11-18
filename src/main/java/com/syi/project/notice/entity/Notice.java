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
  private LocalDate noticeRegDate;

  @LastModifiedDate
  private LocalDate noticeModifyDate;

  private Long viewCount = 0L;

  private boolean isGlobal;

  @OneToMany(mappedBy = "notice", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<NoticeFile> noticeFiles;

  private Long deletedBy;

  public boolean hasFiles() {
    return noticeFiles != null && !noticeFiles.isEmpty();
  }

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
  }

  public void markAsDeleted(Long deletedBy) {
    this.deletedBy = deletedBy;
  }

}
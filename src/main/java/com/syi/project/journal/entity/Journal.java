package com.syi.project.journal.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  private String fileUrl;      // S3에 업로드된 파일의 URL
  private String fileName;     // 원본 파일명
  private String fileSize;     // 파일 크기

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public Journal(Member member, Course course, String title, String content,
      String fileUrl, String fileName, String fileSize) {
    this.member = member;
    this.course = course;
    this.title = title;
    this.content = content;
    this.fileUrl = fileUrl;
    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  // 일지 수정 메소드 (파일은 null 아닐 때 수정가능, null이면 기존 파일 유지됨)
  public void update(String title, String content,
      String fileUrl, String fileName, String fileSize) {
    this.title = title;
    this.content = content;
    if (fileUrl != null) {
      this.fileUrl = fileUrl;
      this.fileName = fileName;
      this.fileSize = fileSize;
    }
  }
}
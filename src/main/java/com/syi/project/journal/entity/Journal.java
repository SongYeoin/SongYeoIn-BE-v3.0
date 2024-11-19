package com.syi.project.journal.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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

  // List<JournalFile> 대신 단일 JournalFile로 변경
  @OneToOne(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
  private JournalFile journalFile;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public Journal(Member member, Course course, String title, String content) {
    this.member = member;
    this.course = course;
    this.title = title;
    this.content = content;
  }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public void setFile(JournalFile journalFile) {
    this.journalFile = journalFile;
  }
}
package com.syi.project.journal.entity;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.entity.BaseTimeEntity;
import com.syi.project.common.enums.Role;
import com.syi.project.course.entity.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journal extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @OneToOne(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotNull(message = "교육일지에는 파일 첨부가 필수입니다")
  private JournalFile journalFile;

  @Column(nullable = false)
  private LocalDate educationDate;  // 실제 교육 진행 날짜

  @Builder
  public Journal(Member member, Course course, String title, String content, LocalDate educationDate) {
    this.member = member;
    this.course = course;
    this.title = title;
    this.content = content;
    this.educationDate = educationDate;
  }

  public boolean isOwner(Long memberId) {
    return this.member.getId().equals(memberId);
  }

  public boolean canAccess(Member member) {
    return member.getRole() == Role.ADMIN || isOwner(member.getId());
  }

  // 파일 설정 메서드 추가
  public void setFile(JournalFile journalFile) {
    this.journalFile = journalFile;
  }

  // 내용 업데이트 메서드 추가
  public void update(String title, String content, LocalDate educationDate) {
    this.title = title;
    this.content = content;
    this.educationDate = educationDate;
  }

  public boolean isValidEducationDate(LocalDate educationDate) {
    return !educationDate.isBefore(this.course.getStartDate()) &&
        !educationDate.isAfter(this.course.getEndDate());
  }
}
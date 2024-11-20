package com.syi.project.enroll.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enroll {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long courseId;
  private Long memberId;
  private Long deletedBy;

  public Enroll(Long courseId, Long memberId) {
    this.courseId = courseId;
    this.memberId = memberId;
    this.deletedBy = null;
  }

  public void deleteEnrollment(Long deletedBy) {
    this.deletedBy = deletedBy;
  }

}

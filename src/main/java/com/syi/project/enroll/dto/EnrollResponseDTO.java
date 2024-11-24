package com.syi.project.enroll.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.syi.project.enroll.entity.Enroll;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class EnrollResponseDTO {

  private Long id;
  private Long courseId;
  private String courseName;  // 과정명
  private String adminName;  // 담당자
  private LocalDate enrollDate;  // 개강일
  private LocalDate endDate;  // 종강일
  private Long memberId;
  private Long deletedBy;

  public EnrollResponseDTO(Enroll enroll) {
    this.id = enroll.getId();
    this.courseId = enroll.getCourseId();
    this.memberId = enroll.getMemberId();
    this.deletedBy = enroll.getDeletedBy();
  }

  @QueryProjection
  public EnrollResponseDTO(Long id, Long courseId, String courseName, String adminName,
      LocalDate enrollDate, LocalDate endDate) {
    this.id = id;
    this.courseId = courseId;
    this.courseName = courseName;
    this.adminName = adminName;
    this.enrollDate = enrollDate;
    this.endDate = endDate;
  }

}

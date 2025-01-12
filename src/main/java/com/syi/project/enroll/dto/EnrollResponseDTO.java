package com.syi.project.enroll.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  private String teacherName;  // 강사 추가
  private LocalDate enrollDate;  // 등록일
  private LocalDate startDate; // 개강일
  private LocalDate endDate;  // 종강일
  private Long memberId;
  private Long deletedBy;

  // name으로도 접근 가능하도록 설정
  @JsonProperty("name")
  public String getName() {
    return this.courseName;
  }

  // courses로도 접근 가능하도록 설정
  @JsonProperty("courses")
  public String getCourses() {
    return this.courseName;
  }

  public EnrollResponseDTO(Enroll enroll) {
    this.id = enroll.getId();
    this.courseId = enroll.getCourseId();
    this.memberId = enroll.getMemberId();
    this.deletedBy = enroll.getDeletedBy();
  }

  @QueryProjection
  public EnrollResponseDTO(Long id, Long courseId, String courseName, String adminName, String teacherName,
      LocalDate enrollDate, LocalDate startDate, LocalDate endDate) {
    this.id = id;
    this.courseId = courseId;
    this.courseName = courseName;
    this.adminName = adminName;
    this.teacherName = teacherName; // 강사 추가
    this.enrollDate = enrollDate;
    this.startDate = startDate;
    this.endDate = endDate;
  }

}

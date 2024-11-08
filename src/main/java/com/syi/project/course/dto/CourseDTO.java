package com.syi.project.course.dto;

import com.syi.project.course.entity.Course;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ToString
public class CourseDTO {

  private Long id;

  private String name;

  private String description;

  private String managerName;

  private String teacherName;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  private String roomName;

  private LocalDate enrollDate;

  private LocalDate modifiedDate;

  private String status;  /* 끝난 과정인지 아닌지 판단*/

  private Boolean isDeleted;

  private int managerNo;

  @Builder
  public CourseDTO(Long id, String name, String description, String managerName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName, LocalDate enrollDate,
      LocalDate modifiedDate, String status, Boolean isDeleted, int managerNo) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.managerName = managerName;
    this.teacherName = teacherName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.roomName = roomName;
    this.enrollDate = enrollDate != null ? enrollDate : LocalDate.now();
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();;
    this.status = status != null ? status : "Y";
    this.isDeleted = isDeleted != null ? isDeleted : false;
    this.managerNo = managerNo;
  }

  /*public Course toEntity(CourseDTO courseDTO){
    return Course.buil
  }*/

}

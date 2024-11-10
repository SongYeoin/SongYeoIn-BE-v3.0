package com.syi.project.course.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ToString
public class CoursePatchDTO {

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

  private Long managerNo;


  @Builder
  public CoursePatchDTO(String name, String description, String managerName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName,
      LocalDate modifiedDate, String status, Long managerNo) {
    this.name = name;
    this.description = description;
    this.managerName = managerName;
    this.teacherName = teacherName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.roomName = roomName;
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();
    ;
    this.status = status;
    this.managerNo = managerNo;
  }

}

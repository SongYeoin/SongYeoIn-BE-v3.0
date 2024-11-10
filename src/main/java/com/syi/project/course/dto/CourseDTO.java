package com.syi.project.course.dto;

import com.syi.project.course.entity.Course;
import jakarta.validation.constraints.NotBlank;
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

  @NotBlank
  private String name;

  @NotBlank
  private String description;

  @NotBlank
  private String managerName;

  @NotBlank
  private String teacherName;

  @NotBlank
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @NotBlank
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  @NotBlank
  private String roomName;


  private LocalDate enrollDate;

  private LocalDate modifiedDate;

  private String status;  /* 끝난 과정인지 아닌지 판단*/

  private Boolean isDeleted;

  @NotBlank
  private Long managerNo;

  @Builder
  public CourseDTO(Long id, String name, String description, String managerName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName, LocalDate enrollDate,
      LocalDate modifiedDate, String status, Boolean isDeleted, Long managerNo) {
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

  public Course toEntity() {
    return new Course(
        this.name,
        this.description,
        this.managerName,
        this.teacherName,
        this.startDate,
        this.endDate,
        this.roomName,
        this.enrollDate,
        this.modifiedDate,
        this.status,
        this.isDeleted,
        this.managerNo);

  }

}

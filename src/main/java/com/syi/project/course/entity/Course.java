package com.syi.project.course.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
/*@Setter*/
@ToString
@NoArgsConstructor
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_id")
  private Long id;

  @NotNull
  private String name;

  @NotNull
  private String description;

  @NotNull
  private String managerName;

  @NotNull
  private String teacherName;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull
  private LocalDate startDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull
  private LocalDate endDate;

  @NotNull
  private String roomName;

  @NotNull
  private LocalDate enrollDate;

  private LocalDate modifiedDate;


  private String status;

  private Boolean isDeleted;

  @NotNull
  private int managerNo;


  @Builder
  public Course(Long id, String name, String description, String managerName, String teacherName,
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
}

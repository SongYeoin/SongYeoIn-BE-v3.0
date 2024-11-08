package com.syi.project.course.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@ToString
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



}

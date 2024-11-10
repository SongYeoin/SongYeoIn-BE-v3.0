package com.syi.project.course.entity;


import com.syi.project.course.dto.CoursePatchDTO;
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
import lombok.ToString;
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
  private Long managerNo;


  /* 교육과정 등록할 때 사용하는 생성자 */
  public Course(String name, String description, String managerName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName, LocalDate enrollDate,
      LocalDate modifiedDate, String status, Boolean isDeleted, Long managerNo) {
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

  /* 수정할 때  dto->entity */
  public void updateWith(CoursePatchDTO dto) {
    if (dto.getName() != null) {
      this.name = dto.getName();
    }
    if (dto.getDescription() != null) {
      this.description = dto.getDescription();
    }
    if (dto.getManagerName() != null) {
      this.managerName = dto.getManagerName();
    }
    if (dto.getTeacherName() != null) {
      this.teacherName = dto.getTeacherName();
    }
    if (dto.getStartDate() != null) {
      this.startDate = dto.getStartDate();
    }
    if (dto.getEndDate() != null) {
      this.endDate = dto.getEndDate();
    }
    if (dto.getRoomName() != null) {
      this.roomName = dto.getRoomName();
    }
    if (dto.getModifiedDate() != null) {
      this.modifiedDate = dto.getModifiedDate();
    }
    if (dto.getStatus() != null) {
      this.status = dto.getStatus();
    }
    if (dto.getIsDeleted() != null) {
      this.isDeleted = dto.getIsDeleted();
    }
    if (dto.getManagerNo() != null) {
      this.managerNo = dto.getManagerNo();
    }
  }

  public void updateIsDeletedToTrue() {
    this.isDeleted = true;
  }

}

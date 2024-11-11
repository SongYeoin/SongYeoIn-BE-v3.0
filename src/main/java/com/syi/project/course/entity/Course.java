package com.syi.project.course.entity;


import com.syi.project.common.annotation.StartBeforeEnd;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.course.dto.CoursePatchDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
/*@Setter*/
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@StartBeforeEnd(message = "시작 날짜는 종료 날짜보다 빨라야 합니다.")
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_id")
  private Long id;

  @NotNull
  @Size(min = 2, max = 20, message = "교육 과정명은 2자 이상 20자 이하이어야 합니다.")
  @Column(nullable = false, unique = true, length = 20)
  private String name;

  @NotNull
  @Size(min = 2, max = 50, message = "교육 과정 설명은 2자 이상 50자 이하이어야 합니다.")
  @Column(nullable = false, length = 50)
  private String description;

  @NotNull
  @Size(min = 2, max = 30, message = "담당자명은 2자 이상 30자 이하이어야 합니다.")
  @Column(nullable = false, length = 30)
  private String managerName;

  @NotNull
  @Size(min = 2, max = 30, message = "강사명은 2자 이상 30자 이하이어야 합니다.")
  @Column(nullable = false, length = 30)
  private String teacherName;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "개강 날짜는 필수입니다.")
  @Column(nullable = false)
  private LocalDate startDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "종강 날짜는 필수입니다.")
  @Column(nullable = false)
  private LocalDate endDate;

  @NotBlank(message = "강의실은 필수입니다.")
  @Size(min = 2, max = 4, message = "강의실은 2자 이상 4자 이하이어야 합니다.")
  @Column(nullable = false, length = 4)
  private String roomName;

  @NotNull
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  @Column(nullable = false)
  private LocalDate enrollDate;

  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDate modifiedDate;

  @Enumerated(EnumType.STRING)
  @Column(length = 1, nullable = false)
  private CourseStatus status;

  private Long deletedBy;

  @NotNull(message = "담당자 번호는 필수입니다.")
  @Column(nullable = false)
  private Long managerId;


  /* 교육과정 등록할 때 사용하는 생성자 */
  public Course(String name, String description, String managerName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName, LocalDate enrollDate,
      LocalDate modifiedDate, CourseStatus status, Long deletedBy, Long managerId) {
    this.name = name;
    this.description = description;
    this.managerName = managerName;
    this.teacherName = teacherName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.roomName = roomName;
    this.enrollDate = enrollDate != null ? enrollDate : LocalDate.now();
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();;
    this.status = status != null ? status : CourseStatus.Y;
    this.deletedBy = deletedBy;
    this.managerId = managerId;
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
    if (dto.getDeletedBy() != null) {
      this.deletedBy = dto.getDeletedBy();
    }
    if (dto.getManagerId() != null) {
      this.managerId = dto.getManagerId();
    }
  }

  /* 삭제한 사람의 id 넣기 */
  public void updateIsDeletedToTrue(Long id) {
    this.deletedBy = id;
  }

}

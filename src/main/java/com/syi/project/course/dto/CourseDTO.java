package com.syi.project.course.dto;

import com.syi.project.course.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
/*@Setter*/
@ToString
@Schema(description = "교육 과정 등록 DTO")
public class CourseDTO {

  @Schema(description = "교육 과정 ID", example = "1")
  private Long id;

  @Schema(description = "교육 과정명", example = "자바 스프링 백엔드 과정")
  @NotBlank(message = "교육 과정명은 필수입니다.")
  @Size(min = 2, max = 20,message = "교육 과정명은 2자 이상 20자 이하이어야 합니다.")
  private String name;

  @Schema(description = "교육과정 설명", example = "자바와 스픠링을 배우고 익힙니다.")
  @NotBlank(message = "교육과정 설명은 필수입니다.")
  @Size(min = 2, max = 50, message = "교육 과정 설명은 2자 이상 50자 이하이어야 합니다.")
  private String description;

  @Schema(description = "담당자명", example = "황정미")
  @NotBlank(message = "담당자명은 필수입니다.")
  private String managerName;

  @Schema(description = "강사명", example = "정민신")
  @NotBlank(message = "강사명은 필수입니다.")
  private String teacherName;

  @Schema(description = "개강 날짜", example = "2024-04-11")
  @NotNull(message = "개강 날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @Schema(description = "종강 날짜", example = "2024-09-16")
  @NotNull(message = "종강 날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  @Schema(description = "강의실", example = "302")
  @NotBlank(message = "강의실은 필수입니다.")
  private String roomName;

  @Schema(description = "등록일", example = "2024-11-11")
  private LocalDate enrollDate;

  @Schema(description = "수정일", example = "2024-11-11")
  private LocalDate modifiedDate;

  @Schema(description = "과정 상태", example = "Y")
  private String status;  /* 끝난 과정인지 아닌지 판단*/

  @Schema(description = "삭제 여부", example = "false")
  private Boolean isDeleted;

  @Schema(description = "담당자 번호", example = "1")
  @NotNull(message = "담당자 번호는 필수입니다.")
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

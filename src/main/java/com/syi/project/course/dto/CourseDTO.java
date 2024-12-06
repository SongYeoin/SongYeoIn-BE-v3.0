package com.syi.project.course.dto;

import com.syi.project.common.annotation.StartBeforeEnd;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.course.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

@Getter
/*@Setter*/
@ToString
@Schema(description = "교육 과정 등록 DTO")
@StartBeforeEnd(message = "종료 날짜는 시작 날짜 이후여야 합니다.")
public class CourseDTO {

  @Getter
  @ToString
  public static class CourseListDTO{
    private Long courseId;
    private String courseName;

    @Builder
    public CourseListDTO(Long courseId, String courseName) {
      this.courseId = courseId;
      this.courseName = courseName;
    }
  }


  public interface Create {

  }

  public interface Update {

  }

  @Schema(description = "교육 과정 ID", example = "1")
  @NotNull(groups = CourseDTO.Update.class)
  @Null(groups = CourseDTO.Create.class)
  private Long id;

  @Schema(description = "교육 과정명", example = "자바 스프링 백엔드 과정")
  @NotBlank(groups = Create.class,message = "교육 과정명은 필수입니다.")
  @Size(min = 2, max = 20,message = "교육 과정명은 2자 이상 20자 이하이어야 합니다.")
  private String name;

  @Schema(description = "교육과정 설명", example = "자바와 스픠링을 배우고 익힙니다.")
  //@NotBlank(message = "교육과정 설명은 필수입니다.")
  @Nullable
  @Size(min = 0, max = 50, message = "교육 과정 설명은 50자 이하이어야 합니다.")
  private String description;

  @Schema(description = "담당자명", example = "황정미")
  @NotBlank(groups = Create.class,message = "담당자명은 필수입니다.")
  @Size(min = 2, max = 30, message = "담당자명은 2자 이상 30자 이하이어야 합니다.")
  private String adminName;

  @Schema(description = "강사명", example = "정민신")
  //@NotBlank(message = "강사명은 필수입니다.")
  @Nullable
  @Size(max = 30, message = "강사명은 30자 이하이어야 합니다.")
  private String teacherName;

  @Schema(description = "개강 날짜", example = "2024-04-11")
  @NotNull(groups = Create.class,message = "개강 날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @Schema(description = "종강 날짜", example = "2024-09-16")
  @NotNull(groups = Create.class,message = "종강 날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  @Schema(description = "강의실", example = "302")
  //@NotBlank(message = "강의실은 필수입니다.")
  @Nullable
  @Size(max = 4, message = "강의실은 4자 이하이어야 합니다.")
  private String roomName;

  @Schema(description = "등록일", example = "2024-11-11")
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDate enrollDate;

  @Schema(description = "수정일", example = "2024-11-11")
  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDate modifiedDate;

  @Schema(description = "과정 상태", example = "Y")
  private CourseStatus status;  /* 끝난 과정인지 아닌지 판단*/

  @Schema(description = "삭제한 memberID", example = "1")
  private Long deletedBy;

  @Schema(description = "담당자 번호", example = "1")
  @NotNull(groups = Create.class,message = "담당자 번호는 필수입니다.")
  private Long adminId;

  @Schema(description = "과정기간", example = "24")
  private Long weeks;

  @Schema(description = "수강생 수", example = "15")
  private Integer counts;

  @Schema(description = "시간표 id", example = "1")
  private Long scheduleId;


  @Builder
  public CourseDTO(Long id, String name, String description, String adminName, String teacherName,
      LocalDate startDate, LocalDate endDate, String roomName, LocalDate enrollDate,
      LocalDate modifiedDate, CourseStatus status, Long deletedBy, Long adminId,Long weeks, Integer counts,Long scheduleId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.adminName = adminName;
    this.teacherName = teacherName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.roomName = roomName;
    this.enrollDate = enrollDate != null ? enrollDate : LocalDate.now();
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();;
    this.status = status != null ? status : CourseStatus.Y;
    this.deletedBy = deletedBy;
    this.adminId = adminId;
    this.weeks = weeks;
    this.counts = counts;
    this.scheduleId = scheduleId;
  }

  public Course toEntity() {
    return new Course(
        this.name,
        this.description,
        this.adminName,
        this.teacherName,
        this.startDate,
        this.endDate,
        this.roomName,
        this.enrollDate,
        this.modifiedDate,
        this.status,
        this.deletedBy,
        this.adminId);

  }

  public static CourseDTO fromEntity(Course course, Integer studentCounts) {
    // 수강 기산 주 계산하기
    long days = ChronoUnit.DAYS.between(course.getStartDate(), course.getEndDate());
    long weeks = (days + 6) / 7; // 올림 처리된 주 수 계산


    return CourseDTO.builder()
        .id(course.getId())
        .name(course.getName())
        .description(course.getDescription())
        .adminName(course.getAdminName())
        .teacherName(course.getTeacherName())
        .startDate(course.getStartDate())
        .endDate(course.getEndDate())
        .roomName(course.getRoomName())
        .enrollDate(course.getEnrollDate())
        .modifiedDate(course.getModifiedDate())
        .adminId(course.getAdminId())
        .weeks(weeks)
        .counts(studentCounts)
        .scheduleId(course.getScheduleId())
        .build();
  }


}

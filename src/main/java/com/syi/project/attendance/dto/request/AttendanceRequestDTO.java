package com.syi.project.attendance.dto.request;

import com.syi.project.attendance.entity.Attendance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@ToString
@Schema(description = "출석 요청 DTO")
public class AttendanceRequestDTO {

  public interface Update {

  }

  @Schema(description = "출석 ID", example = "1")
  @NotNull(groups = Update.class, message = "출석 ID는 필수입니다.")
  private Long attendanceId;

  @Schema(description = "출석 상태", example = "")
  @NotNull(groups = Update.class, message = "출석 상태는 필수입니다.")
  private String status;

  @Schema(description = "출석 날짜", example = "")
  private LocalDate date;

  @Schema(description = "출석 등록일", example = "")
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDateTime enrollDate;

  @Schema(description = "출석 수정일", example = "")
  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDateTime modifiedDate;

  @Schema(description = "교시 ID", example = "1")
  @NotNull(message = "교시 ID는 필수입니다.")
  private Long periodId;

  @Schema(description = "교육과정 ID", example = "1")
  @NotNull(message = "교육과정 ID는 필수입니다.")
  private Long courseId;

  @Schema(description = "수강생 ID", example = "1")
  @NotNull(message = "수강생 ID는 필수입니다.")
  private Long memberId;

  @Size(max = 255, message = "메모는 최대 255자까지 가능합니다.")
  private String memo;

  /* 필터링 조건들 */
  private String studentName;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  /* 결석으로 변경하기 위한 수강생 id 리스트 */
  private List<Long> attendanceIds;


  @Builder
  public AttendanceRequestDTO(Long attendanceId, String status, LocalDate date,
      LocalDateTime enrollDate, LocalDateTime modifiedDate, Long periodId, Long courseId,
      Long memberId, String memo, String studentName,LocalDate startDate,LocalDate endDate,List<Long> attendanceIds) {
    this.attendanceId = attendanceId;
    this.status = status;
    this.date = date;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
    this.studentName = studentName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.attendanceIds = attendanceIds;
  }


  public Attendance toEntity() {
    return new Attendance(
        null,
        null,
        null,
        null,
        null,
        this.periodId,
        this.courseId,
        this.memberId,
        this.memo
    );
  }
}

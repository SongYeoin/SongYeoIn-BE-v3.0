package com.syi.project.attendance.dto.response;

import com.syi.project.attendance.entity.Attendance;
import com.syi.project.common.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "출석 응답 DTO")
public class AttendanceResponseDTO {

  public interface Update {

  }

  @Schema(description = "출석 ID", example = "1")
  @NotNull(groups = Update.class, message = "출석 ID는 필수입니다.")
  private Long attendanceId;

  @Schema(description = "출석 상태", example = "")
  @NotNull(groups = Update.class, message = "출석 상태는 필수입니다.")
  private AttendanceStatus status;

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

  @Schema(description = "예외시 표시 메시지", example = "학원 네트워크에서만 출석이 가능합니다.")
  private String resultMessage;


  @Builder
  public AttendanceResponseDTO(Long attendanceId, AttendanceStatus status, LocalDate date,
      LocalDateTime enrollDate, LocalDateTime modifiedDate, Long periodId, Long courseId,
      Long memberId, String memo ,String resultMessage) {
    this.attendanceId = attendanceId;
    this.status = status;
    this.date = date;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
    this.resultMessage = resultMessage;
  }

  public static AttendanceResponseDTO fromEntity(Attendance attendance) {
    return AttendanceResponseDTO.builder()
        .attendanceId(attendance.getId())
        .status(attendance.getStatus())
        .date(attendance.getDate())
        .enrollDate(attendance.getEnrollDate())
        .modifiedDate(attendance.getModifiedDate())
        .periodId(attendance.getPeriodId())
        .courseId(attendance.getPeriodId())
        .memberId(attendance.getMemberId())
        .memo(attendance.getMemo())
        .build();
  }
  public static AttendanceResponseDTO withMessage(String message) {
    return AttendanceResponseDTO.builder()
        .resultMessage(message)
        .build();
  }
}

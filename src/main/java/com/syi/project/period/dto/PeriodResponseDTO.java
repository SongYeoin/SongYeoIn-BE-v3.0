package com.syi.project.period.dto;

import com.syi.project.period.eneity.Period;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "교시 응답 DTO")
public class PeriodResponseDTO {

  @Schema(description = "교시 ID", example = "1")
  @NotNull(message = "교시 ID는 필수여야 합니다.")
  private Long id;

  @Schema(description = "교육과정 ID", example = "1")
  @NotNull(message = "교육과정 ID는 필수여야 합니다.")
  private Long courseId;

  @Schema(description = "시간표 ID", example = "1")
  @NotNull(message = "시간표 ID는 필수여야 합니다.")
  private Long scheduleId;

  @Schema(description = "요일", example = "월요일")
  @NotNull(message = "요일은 필수여야 합니다.")
  private String dayOfWeek;

  @Schema(description = "교시 이름", example = "1교시")
  @NotNull(message = "교시 이름은 필수여야 합니다.")
  private String name;

  @Schema(description = "시작 시간", example = "14:00")
  @NotNull(message = "시작 시간은 필수여야 합니다.")
  private LocalTime startTime;

  @Schema(description = "종료 시간", example = "15:30")
  @NotNull(message = "종료 시간은 필수여야 합니다.")
  private LocalTime endTime;

  @Schema(description = "삭제한 memberID", example = "1")
  private Long deletedBy;

  @Builder
  public PeriodResponseDTO(Long id, Long courseId, Long scheduleId, String dayOfWeek,
      String name, LocalTime startTime, LocalTime endTime, Long deletedBy) {
    this.id = id;
    this.courseId = courseId;
    this.scheduleId = scheduleId;
    this.dayOfWeek = dayOfWeek;
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
    this.deletedBy = deletedBy;
  }

  public static PeriodResponseDTO fromEntity(Period period) {
    return PeriodResponseDTO.builder()
        .id(period.getId())
        .courseId(period.getCourseId())
        .scheduleId(period.getScheduleId())
        .dayOfWeek(period.getDayOfWeek())
        .name(period.getName())
        .startTime(period.getStartTime())
        .endTime(period.getEndTime())
        .deletedBy(period.getDeletedBy())
        .build();
  }

}

package com.syi.project.period.dto;

import com.syi.project.period.eneity.Period;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "교시 요청 DTO")
public class PeriodRequestDTO {

  @Schema(description = "교시 ID", example = "1")
  private Long id;

  @Schema(description = "요일", example = "월요일")
  @NotEmpty(message = "요일을 선택해야 합니다.")
  private String dayOfWeek;

  @Schema(description = "교시 이름", example = "1교시")
  @NotNull(message = "교시 이름을 입력해야 합니다.")

  private String periodName;

  @Schema(description = "시작 시간", example = "14:00")
  @NotNull(message = "시작 시간을 입력해야 합니다.")
  private LocalTime startTime;

  @Schema(description = "종료 시간", example = "15:30")
  @NotNull(message = "종료 시간을 입력해야 합니다.")
  private LocalTime endTime;

  @Schema(description = "삭제한 memberID", example = "1")
  private Long deletedBy;


  @Builder
  public PeriodRequestDTO(Long id, String dayOfWeek, String name, LocalTime startTime,
      LocalTime endTime, Long deletedBy) {
    this.id = id;
    this.dayOfWeek = dayOfWeek;
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
    this.deletedBy = deletedBy;
  }

  public Period toEntity(Long courseId, Long scheduleId) {
    return new Period(
        null,
        courseId,
        scheduleId,
        this.dayOfWeek,
        this.name,
        this.startTime,
        this.endTime,
        this.deletedBy);
  }

  public Period toEntityForPatch() {
    return new Period(
        this.dayOfWeek,
        this.startTime,
        this.endTime);
  }


}

package com.syi.project.schedule.dto;

import com.syi.project.period.dto.PeriodRequestDTO;
import com.syi.project.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "시간표 요청 DTO")
public class ScheduleRequestDTO {

  @Schema(description = "시간표 ID", example = "1")
  private Long id;

  @Schema(description = "시간표 등록일", example = "")
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDate enrollDate;

  @Schema(description = "시간표 수정일", example = "")
  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDate modifiedDate;

  @Schema(description = "삭제한 memberID", example = "1")
  private Long deletedBy;

  @Schema(description = "교육 과정 ID", example = "1")
  @NotNull(message = "교육과정 ID는 필수입니다.")
  private Long courseId;

  @Schema(description = "교시 정보 List", example = "")
  @NotNull(message = "교시 정보 List는 필수입니다.")
  private List<PeriodRequestDTO> periods;

  @Builder
  public ScheduleRequestDTO(Long id, LocalDate enrollDate, LocalDate modifiedDate, Long deletedBy,
      Long courseId, List<PeriodRequestDTO> periods) {
    this.id = id;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.deletedBy = deletedBy;
    this.courseId = courseId;
    this.periods = periods;
  }

  public Schedule toEntity() {
    return new Schedule(
        this.enrollDate,
        this.modifiedDate,
        this.deletedBy,
        this.courseId
    );
  }


}

package com.syi.project.schedule.dto;

import com.syi.project.period.dto.PeriodRequestDTO;
import com.syi.project.period.dto.PeriodResponseDTO;
import com.syi.project.period.eneity.Period;
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
@Schema(description = "시간표 응답 DTO")
public class ScheduleResponseDTO {

  /* 시간표 수정 요청 시 */
  @Getter
  @ToString
  public static class ScheduleUpdateResponseDTO{
    private Long scheduleId;
    private List<PeriodResponseDTO> updatedPeriods; // 수정된 교시
    private List<PeriodResponseDTO> newPeriods; // 새로 추가된 교시
    /*private List<Long> deletedPeriodIds; // 삭제된 교시 ID*/

    @Builder
    public ScheduleUpdateResponseDTO(Long scheduleId,List<PeriodResponseDTO> updatedPeriods,
        List<PeriodResponseDTO> newPeriods) {
      this.scheduleId = scheduleId;
      this.updatedPeriods = updatedPeriods;
      this.newPeriods = newPeriods;
      //this.deletedPeriodIds = deletedPeriodIds;
    }
  }


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
  private List<PeriodResponseDTO> periods;

  @Builder
  public ScheduleResponseDTO(Long id, LocalDate enrollDate, LocalDate modifiedDate, Long deletedBy,
      Long courseId, List<PeriodResponseDTO> periods) {
    this.id = id;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.deletedBy = deletedBy;
    this.courseId = courseId;
    this.periods = periods;
  }

  public static ScheduleResponseDTO fromEntity(Schedule schedule, List<Period> periodList) {

    List<PeriodResponseDTO> periodDTOs = null;
    if (periodList != null) {
      periodDTOs = periodList.stream()
          .map(PeriodResponseDTO::fromEntity)
          .toList();
    }

    return ScheduleResponseDTO.builder()
        .id(schedule.getId())
        .enrollDate(schedule.getEnrollDate())
        .modifiedDate(schedule.getModifiedDate())
        .deletedBy(schedule.getDeletedBy())
        .courseId(schedule.getCourseId())
        .periods(periodDTOs)
        .build();
  }

}

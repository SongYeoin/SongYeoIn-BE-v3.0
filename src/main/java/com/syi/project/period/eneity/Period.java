package com.syi.project.period.eneity;


import com.syi.project.common.annotation.StartBeforeEnd;
import com.syi.project.period.dto.PeriodRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@StartBeforeEnd(message = "시작 시간은 종료 시간보다 빨라야 합니다.")
public class Period {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "period_id")
  private Long id;

  @NotNull
  @Column(nullable = false)
  private Long courseId;

  @NotNull
  @Column(nullable = false)
  private Long scheduleId;


  @NotBlank(message = "요일을 입력해주세요.")
  @Size(min = 1, max = 3, message = "요일은 최대 3자까지 가능합니다.")
  @Column(nullable = false, length = 3)
  private String dayOfWeek;

  @NotBlank(message = "교시명을 입력해주세요.")
  @Size(min = 1, max = 3, message = "교시명은 최대 3자까지 가능합니다.")
  @Column(nullable = false, length = 3)
  private String periodName;

  //@NotBlank(message = "시작 시간을 입력해주세요.")
  @NotNull(message = "시작 시간을 입력해주세요.")
  //@Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식은 HH:mm이어야 합니다.")
  @Column(nullable = false, length = 5)
  private LocalTime startTime;

  @NotNull(message = "종료 시간을 입력해주세요.")
  //@NotBlank(message = "종료 시간을 입력해주세요.")
  //@Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식은 HH:mm이어야 합니다.")
  @Column(nullable = false, length = 5)
  private LocalTime endTime;

  private Long deletedBy;

  public Period(Long id,Long courseId, Long scheduleId, String dayOfWeek, String periodName,
      LocalTime startTime, LocalTime endTime, Long deletedBy) {
    this.id = id;
    this.courseId = courseId;
    this.scheduleId = scheduleId;
    this.dayOfWeek = dayOfWeek;
    this.periodName = periodName;
    this.startTime = startTime;
    this.endTime = endTime;
    this.deletedBy = deletedBy;
  }

  public Period(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void updateWith(PeriodRequestDTO dto) {
    if (dto.getDayOfWeek() != null) {
      this.dayOfWeek = dto.getDayOfWeek();
    }

    if (dto.getStartTime() != null) {
      this.startTime = dto.getStartTime();
    }

    if (dto.getEndTime() != null) {
      this.endTime = dto.getEndTime();
    }

  }

  public void updateDeletedBy(Long id) {
    this.deletedBy = id;
  }


}

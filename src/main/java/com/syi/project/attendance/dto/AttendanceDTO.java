package com.syi.project.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "출석 기본 DTO")
public class AttendanceDTO {

  @Schema(description = "출석 ID", example = "1")
  private Long id;

  @Schema(description = "출석 상태", example = "")
  private String status;

  @Schema(description = "출석 날짜", example = "2024-11-24")
  private LocalDate date;

  @Schema(description = "출석 등록일", example = "")
  private LocalDateTime enrollDate;

  @Schema(description = "출석 수정일", example = "")
  private LocalDateTime modifiedDate;

  @Schema(description = "교시 ID", example = "1")
  private Long periodId;

  @Schema(description = "교육과정 ID", example = "1")
  private Long courseId;

  @Schema(description = "수강생 ID", example = "1")
  private Long memberId;

  @Schema(description = "메모", example = "수강생 네트워크 문제로 출석 상태 수정(지각->출석)")
  private String memo;


  private String periodName;

  @Builder
  public AttendanceDTO(Long id, String status, LocalDate date, LocalDateTime enrollDate,
      LocalDateTime modifiedDate, Long periodId, Long courseId, Long memberId, String memo,
      String periodName) {
    this.id = id;
    this.status = status;
    this.date = date;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
    this.periodName = periodName;
  }
}

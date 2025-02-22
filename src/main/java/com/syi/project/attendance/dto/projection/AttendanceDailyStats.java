package com.syi.project.attendance.dto.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AttendanceDailyStats {
  private Long studentId;
  private LocalDate date;
  private int totalSessions;
  private int lateCount;
  private int absentCount;
  private int earlyLeaveCount;

  @QueryProjection
  public AttendanceDailyStats(Long studentId, LocalDate date, int totalSessions, int lateCount, int absentCount, int earlyLeaveCount) {
    this.studentId = studentId;
    this.date = date;
    this.totalSessions = totalSessions;
    this.lateCount = lateCount;
    this.absentCount = absentCount;
    this.earlyLeaveCount = earlyLeaveCount;
  }
}

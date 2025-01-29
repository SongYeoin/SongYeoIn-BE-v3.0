package com.syi.project.attendance.dto.projection;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class AttendanceDailyStats {
  private LocalDate date;
  private long totalSessions;
  private long lateCount;
  private long absentCount;

  @QueryProjection
  public AttendanceDailyStats(LocalDate date, long totalSessions, long lateCount, long absentCount) {
    this.date = date;
    this.totalSessions = totalSessions;
    this.lateCount = lateCount;
    this.absentCount = absentCount;
  }
}

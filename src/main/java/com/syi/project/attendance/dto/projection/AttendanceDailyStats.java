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
  private long totalSessions;
  private long lateCount;
  private long absentCount;

  @QueryProjection
  public AttendanceDailyStats(Long studentId, LocalDate date, long totalSessions, long lateCount, long absentCount) {
    this.studentId = studentId;
    this.date = date;
    this.totalSessions = totalSessions;
    this.lateCount = lateCount;
    this.absentCount = absentCount;
  }
}

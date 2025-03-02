package com.syi.project.attendance.entity;

import com.syi.project.common.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Long id;

  //@NotNull(message = "상태값은 필수입니다.")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttendanceStatus status;

  @NotNull(message = "날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column(nullable = false)
  private LocalDate date;

  @NotNull(message = "등록일은 필수입니다.")
  @DateTimeFormat(pattern = "HH:mm:ss yyyy-MM-dd")
  @Column(nullable = false)
  private LocalDateTime enrollDate;

  @DateTimeFormat(pattern = "HH:mm:ss yyyy-MM-dd")
  @Column(nullable = false)
  private LocalDateTime modifiedDate;

  @NotNull(message = "교시 ID는 필수입니다.")
  @Column(nullable = false)
  private Long periodId;

  @NotNull(message = "교육과정 ID는 필수입니다.")
  @Column(nullable = false)
  private Long courseId;

  @NotNull(message = "수강생 ID는 필수입니다.")
  @Column(nullable = false)
  private Long memberId;

  @Size(max = 255, message = "메모는 최대 255자까지 가능합니다.")
  private String memo;

  @DateTimeFormat(pattern = "HH:mm:ss yyyy-MM-dd")
  @Column
  private LocalDateTime enterTime; // 입실 시간

  @DateTimeFormat(pattern = "HH:mm:ss yyyy-MM-dd")
  @Column
  private LocalDateTime exitTime; // 퇴실 시간

  public Attendance(Long id, AttendanceStatus status, LocalDate date, LocalDateTime enrollDate,
      LocalDateTime modifiedDate, Long periodId, Long courseId, Long memberId, String memo, LocalDateTime enterTime, LocalDateTime exitTime) {
    this.id = id;
    this.status = status;
    this.date = date != null ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));
    this.enrollDate = enrollDate != null ? enrollDate : LocalDateTime.now();
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDateTime.now();
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
    this.enterTime = enterTime != null ? enterTime : LocalDateTime.now();
    this.exitTime = exitTime != null ? exitTime : LocalDateTime.now();
  }

  public void updateStatus(AttendanceStatus status){
    this.status = status;
    this.modifiedDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
  }
  //public void updateModifiedDate(LocalDateTime modifiedDate){this.modifiedDate = modifiedDate;}
  // 입실 시간 업데이트
  public void updateEnterTime(LocalDateTime enterTime) {
    this.enterTime = enterTime;
    this.modifiedDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
  }

  // 퇴실 시간 업데이트
  public void updateExitTime(LocalDateTime exitTime) {
    this.exitTime = exitTime;
    this.modifiedDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
  }

}

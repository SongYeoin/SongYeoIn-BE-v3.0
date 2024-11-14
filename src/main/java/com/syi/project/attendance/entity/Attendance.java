package com.syi.project.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Long id;

  @NotNull(message = "상태값은 필수입니다.")
  @Column(nullable = false)
  private String status;

  @NotNull(message = "날짜는 필수입니다.")
  @Column(nullable = false)
  private LocalDateTime date;

  @NotNull(message = "등록일은 필수입니다.")
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  @Column(nullable = false)
  private LocalDateTime enrollDate;

  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
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

  public Attendance(Long id, String status, LocalDateTime date, LocalDateTime enrollDate,
      LocalDateTime modifiedDate, Long periodId, Long courseId, Long memberId, String memo) {
    this.id = id;
    this.status = status;
    this.date = date;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
  }


}

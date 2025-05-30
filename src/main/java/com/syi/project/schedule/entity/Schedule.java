package com.syi.project.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "schedule_id")
  private Long id;

  @NotNull
  //@PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  @Column(nullable = false)
  private LocalDate enrollDate;

  //@PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  @Column(nullable = false)
  private LocalDate modifiedDate;

  private Long deletedBy;

  /*private List<Long> periodIds = new ArrayList<>();*/

  @NotNull
  @Column(nullable = false)
  private Long courseId;

  /* 시간표 등록할 때 사용하는 생성자 */
  public Schedule(LocalDate enrollDate, LocalDate modifiedDate, Long deletedBy,
      Long courseId) {
    this.enrollDate = enrollDate != null ? enrollDate : LocalDate.now();
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();
    this.deletedBy = deletedBy;
    this.courseId = courseId;
  }


  public void updateDeletedBy(Long id) {
    this.deletedBy = id;
  }

  public void updateModifiedDate(LocalDate modifiedDate) {
    this.modifiedDate = modifiedDate != null ? modifiedDate : LocalDate.now();
  }
}

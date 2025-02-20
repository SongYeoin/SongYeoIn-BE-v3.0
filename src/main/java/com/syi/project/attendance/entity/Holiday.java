package com.syi.project.attendance.entity;

import com.syi.project.common.enums.HolidayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "holidays")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "holidays_id")
  private Long id;

  @NotNull(message = "날짜는 필수입니다.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column(nullable = false, unique = true)
  private LocalDate date;

  @Column(nullable = false, length = 100)
  private String name; // 공휴일 이름 (예: 설날, 대체공휴일)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HolidayType type; // 공휴일 유형 (공휴일, 대체공휴일, 주말, 평일)

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
      .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(); // 생성 시간

  public Holiday(LocalDate date, String name, HolidayType type) {
    this.date = date;
    this.name = name;
    this.type = type;
    this.createdAt = LocalDateTime.now();
  }



}

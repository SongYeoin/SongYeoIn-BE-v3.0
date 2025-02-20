package com.syi.project.common.enums;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
  PRESENT("출석"),
  ABSENT("결석"),
  LATE("지각"),
  EARLY_EXIT("조퇴");

  // status 값을 반환하는 메서드
  private final String status;

  // 생성자
  AttendanceStatus(String status) {
    this.status = status;
  }

  // 영어 문자열을 AttendanceStatus Enum으로 변환하는 메서드
  public static AttendanceStatus fromENStatus(String status) {
    for (AttendanceStatus attendanceStatus : values()) {
      if (attendanceStatus.name().equalsIgnoreCase(status)) {
        return attendanceStatus;
      }
    }
    throw new IllegalArgumentException("Unknown status: " + status);
  }

  public String toKorean() {
    return this.status;
  }



}

package com.syi.project.common.enums;

public enum AttendanceStatus {
  PRESENT("출석"),
  ABSENT("결석"),
  LATE("지각");

  private final String status;

  // 생성자
  AttendanceStatus(String status) {
    this.status = status;
  }

  // status 값을 반환하는 메서드
  public String getStatus() {
    return status;
  }

  // String 값을 Enum으로 변환하는 유틸리티 메서드
  public static AttendanceStatus fromStatus(String status) {
    for (AttendanceStatus attendanceStatus : values()) {
      if (attendanceStatus.getStatus().equals(status)) {
        return attendanceStatus;
      }
    }
    throw new IllegalArgumentException("Unknown status: " + status);
  }

}

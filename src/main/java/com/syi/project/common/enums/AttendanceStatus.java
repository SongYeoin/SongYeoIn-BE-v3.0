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

}

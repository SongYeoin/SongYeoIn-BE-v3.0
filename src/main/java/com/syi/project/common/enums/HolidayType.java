package com.syi.project.common.enums;

public enum HolidayType {
  HOLIDAY,  // 공휴일
  SUBSTITUTE_HOLIDAY, // 대체공휴일
  WEEKEND,  // 주말
  WEEKDAY; // 평일


  public HolidayType convertToHolidayType(String name) {
    return switch (name) {
      case "공휴일" -> HolidayType.HOLIDAY;
      case "대체공휴일" -> HolidayType.SUBSTITUTE_HOLIDAY;
      case "주말" -> HolidayType.WEEKEND;
      default -> HolidayType.WEEKDAY;
    };
  }
}
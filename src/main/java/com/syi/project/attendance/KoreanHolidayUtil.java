package com.syi.project.attendance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class KoreanHolidayUtil {
  /**
   * 특정 연도의 한국 공휴일을 계산하는 메서드 (대체공휴일 포함)
   * @param year 연도
   * @return 해당 연도의 공휴일 리스트
   */
  public static Set<LocalDate> getHolidays(int year) {
    Map<String, LocalDate> holidayMap = new HashMap<>();

    // 1. 양력 기반 공휴일 추가
    holidayMap.put("신정", LocalDate.of(year, 1, 1));
    holidayMap.put("삼일절", LocalDate.of(year, 3, 1));
    holidayMap.put("어린이날", LocalDate.of(year, 5, 5));
    holidayMap.put("현충일", LocalDate.of(year, 6, 6));
    holidayMap.put("광복절", LocalDate.of(year, 8, 15));
    holidayMap.put("개천절", LocalDate.of(year, 10, 3));
    holidayMap.put("한글날", LocalDate.of(year, 10, 9));
    holidayMap.put("성탄절", LocalDate.of(year, 12, 25));

    // 2. 음력 기반 공휴일 (설날, 추석, 석가탄신일)
    /*holidayMap.put("설날", getLunarHoliday(year, "설날"));
    holidayMap.put("설날연휴1", getLunarHoliday(year, "설날연휴1"));
    holidayMap.put("설날연휴2", getLunarHoliday(year, "설날연휴2"));
    holidayMap.put("추석", getLunarHoliday(year, "추석"));
    holidayMap.put("추석연휴1", getLunarHoliday(year, "추석연휴1"));
    holidayMap.put("추석연휴2", getLunarHoliday(year, "추석연휴2"));
    holidayMap.put("석가탄신일", getLunarHoliday(year, "석가탄신일"));*/

// 3. 공휴일 리스트 생성 (원본 유지)
    Set<LocalDate> finalHolidays = new HashSet<>(holidayMap.values());

    // 4. 중복 공휴일(같은 날짜) 확인 및 대체 공휴일 처리
    Map<LocalDate, Integer> holidayCount = new HashMap<>();

    for (LocalDate holiday : finalHolidays) {
      holidayCount.put(holiday, holidayCount.getOrDefault(holiday, 0) + 1);
    }

    Set<LocalDate> alternativeHolidays = new HashSet<>();
    for (LocalDate holiday : finalHolidays) {
      if (holidayCount.get(holiday) > 1) {
        // 공휴일이 겹친다면 대체 공휴일 적용
        LocalDate alternativeDate = getNextAvailableWeekday(holiday, finalHolidays);

        // 기존 공휴일이 아니라면 추가
        if (!finalHolidays.contains(alternativeDate)) {
          alternativeHolidays.add(alternativeDate);
          log.debug("{} 공휴일이 겹쳐서 {}로 대체됨", holiday, alternativeDate);
        }
      }
    }

    finalHolidays.addAll(alternativeHolidays);
    log.debug("최종 공휴일 리스트: {}", finalHolidays);
    return finalHolidays;


    /*// 3. 모든 공휴일 추가
    Set<LocalDate> holidays = new HashSet<>(holidayMap.values());

    // 4. 대체 공휴일 처리
    Set<LocalDate> alternativeHolidays = new HashSet<>();
    for (Map.Entry<String, LocalDate> entry : holidayMap.entrySet()) {
      LocalDate holiday = entry.getValue();
      if (holiday.getDayOfWeek() == DayOfWeek.SATURDAY || holiday.getDayOfWeek() == DayOfWeek.SUNDAY || holidays.contains(holiday)) {
        LocalDate alternativeDate = getNextAvailableWeekday(holiday, holidays);
        alternativeHolidays.add(alternativeDate);
      }
    }

    holidays.addAll(alternativeHolidays);
    log.debug("holidays : {}", holidays);
    return holidays;*/
  }

  /**
   * 특정 날짜의 다음 평일을 찾는 메서드 (이미 공휴일이면 또다시 이동)
   */
  private static LocalDate getNextAvailableWeekday(LocalDate date, Set<LocalDate> holidays) {
    log.info("대체 공휴일 판단 함수");
    LocalDate alternativeDate = date.plusDays(1);
    while (alternativeDate.getDayOfWeek() == DayOfWeek.SATURDAY || alternativeDate.getDayOfWeek() == DayOfWeek.SUNDAY || holidays.contains(alternativeDate)) {
      alternativeDate = alternativeDate.plusDays(1);
    }
    log.debug("대체 공휴일 적용:{}", alternativeDate);
    return alternativeDate;
  }

  /**
   * 음력 기반 공휴일을 양력으로 변환하는 메서드
   * @param year 연도
   * @param holidayName 공휴일 이름 (설날, 추석, 석가탄신일)
   * @return 변환된 양력 날짜
   */
  /*public static LocalDate getLunarHoliday(int year, String holidayName) {
    KoreanLunarCalendar calendar = new KoreanLunarCalendar();

    switch (holidayName) {
      case "설날":
        calendar.setLunar(year, 1, 1, false);
        break;
      case "설날연휴1":
        calendar.setLunar(year, 1, 2, false);
        break;
      case "설날연휴2":
        calendar.setLunar(year, 12, 30, false);
        break;
      case "추석":
        calendar.setLunar(year, 8, 15, false);
        break;
      case "추석연휴1":
        calendar.setLunar(year, 8, 16, false);
        break;
      case "추석연휴2":
        calendar.setLunar(year, 8, 14, false);
        break;
      case "석가탄신일":
        calendar.setLunar(year, 4, 8, false);
        break;
      default:
        return null;
    }

    calendar.convertLunarToSolar();
    return LocalDate.of(calendar.getSolarYear(), calendar.getSolarMonth(), calendar.getSolarDay());
  }*/

}

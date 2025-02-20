
package com.syi.project.attendance;

import com.syi.project.attendance.dto.projection.AttendanceDailyStats;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttendanceCalculator {

  // 참여수당 (20일)
  private static final int SEGMENT_DAYS = 20;
  private static final int TOTAL_SESSIONS_PER_DAY = 8; // 하루 8교시
  private static final int LATE_THRESHOLD = 3;

  /**
   * 출석률을 계산하는 메서드 (주말, 공휴일 제외 + 지각 3회 누적 시 결서 1일 적용)
   *
   * @param dailyStats 학생의 출석 데이터 목록
   * @param startDate  교육 과정 시작일
   * @param endDate    교육 과정 종료일
   * @return 전체 출석률과 20일 단위 출석률을 포함하는 맵
   */

  // 115일 전체 출석률 및 20일 단위 출석률 계산
  public static Map<String, Object> calculateAttendanceRates(List<AttendanceDailyStats> dailyStats,
      LocalDate startDate, LocalDate endDate, Set<LocalDate> holidays) {

    log.info("출석률 계산 시작");

    // 첫 날 제외
    List<LocalDate> validDays = getValidDays(startDate.plusDays(1), endDate, holidays);


    // 20일 단위 차수 계산
    List<Map<String, Object>> twentyDaySegments = calculateTwentyDaySegments(validDays);
    // 20일 단위 출석률 리스트
    List<Map<String, Object>> segmentAttendanceRates = new ArrayList<>();

    int totalAttendanceDays = 0; // 학생이 실제 출석한 총 일수
    //int validAttendanceDays = 0; // 출석 가능한 총 일수 (주말/공휴일 제외) - 교육과정 기간
    List<Map<String, Object>> twentyDayRateDetails = new ArrayList<>(); // 20일 단위 출석률 저장 리스트

    int twentyDayAttendanceDays = 0;  // 20일 중 실제 출석일수
    int accumulatedLateCount = 0;   // 지각 횟수 누적

    // 20일 단위 출석률 기간 미리 설정
/*    List<Map<String, Object>> twentyDayPeriods = calculateTwentyDayPeriods(startDate.plusDays(1), endDate,
        holidays);*/

/*    // 유효한 출석 일수 계산
    validAttendanceDays = twentyDayPeriods.stream()
        .mapToInt(period -> {
          LocalDate start = LocalDate.parse((String) period.get("startDate"));
          LocalDate end = LocalDate.parse((String) period.get("endDate"));
          return (int) start.datesUntil(end.plusDays(1))
              .filter(d -> !(d.getDayOfWeek() == DayOfWeek.SATURDAY
                  || d.getDayOfWeek() == DayOfWeek.SUNDAY || holidays.contains(d)))
              .count();
        })
        .sum();

    log.debug("유효한 출석 가능 일수 - 교육과정 기간: {}일", validAttendanceDays);*/

    dailyStats.sort(Comparator.comparing(AttendanceDailyStats::getDate)); // 날짜순으로 정렬

    int processedDays = 0;  // 처리한 출석 가능 일수
    int periodIndex = 0;    // 현재 20일 차수 인덱스

    for (AttendanceDailyStats stats : dailyStats) {
      LocalDate date = stats.getDate();

      // ✅ 첫날 제외
      if (date.isEqual(startDate)) {
        continue;
      }

      // 주말 및 공휴일 제외
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        continue;
      }
      if (holidays.contains(date)) {
        continue;
      }

      // 출석 인정 여부
      boolean isAbsent = stats.getAbsentCount() == 8; // 하루 8교시 전부 결석이면 결석 처리

      if (!isAbsent) {
        accumulatedLateCount += stats.getLateCount(); // 지각 횟수 누적 => 지각은 하루에 한번밖에 가능하지 않음

        if (accumulatedLateCount >= LATE_THRESHOLD) {
          isAbsent = true; // 지각이 누적 3회 이상이면 결석 처리
          accumulatedLateCount -= LATE_THRESHOLD; // 결석 1일 적용 후 남은 지각 횟수 차감
        }
      }

      if (!isAbsent) {
        totalAttendanceDays++;  // 전체 출석일 증가
        twentyDayAttendanceDays++;    // 20일 단위 출석일 증가
      }

      processedDays++;

      // 20일 단위 출석률 계산
      if (processedDays % SEGMENT_DAYS == 0 || periodIndex == twentyDaySegments.size() - 1
          || processedDays < SEGMENT_DAYS) {
        Map<String, Object> period = twentyDaySegments.get(periodIndex);
        double twentyDayRate = roundToTwoDecimalPlaces(
            (twentyDayAttendanceDays / (double) SEGMENT_DAYS) * 100);

        twentyDayRateDetails.add(Map.of(
            "periodIndex", period.get("periodIndex"),
            "startDate", period.get("startDate").toString(),
            "endDate", period.get("endDate").toString(),
            "twentyDayRate", twentyDayRate
        ));

        // ⛔ 20일이 다 차지 않은 경우에는 초기화하지 않음
        if (processedDays % SEGMENT_DAYS == 0) {
          twentyDayAttendanceDays = 0;
          periodIndex++;
        }
      }
    }

    int validAttendanceDays = validDays.size();
    // 전체 출석률 계산
/*    double overallAttendanceRate = validAttendanceDays ? 100.0 :
        roundToTwoDecimalPlaces((totalAttendanceDays / (double) validAttendanceDays) * 100);*/

    double overallAttendanceRate = roundToTwoDecimalPlaces((totalAttendanceDays / (double) validAttendanceDays) * 100);

    log.debug("전체 출석 가능 일수: {}", validAttendanceDays);
    log.debug("전체 출석률: {}", overallAttendanceRate);
    log.debug("20일 단위 출석률 리스트: {}", twentyDayRateDetails);
    Map<String, Object> twentyDayRateDetail = twentyDayRateDetails.get(
        twentyDayRateDetails.size() - 1);
    log.debug("해당하는 20일 단위 출석률: {}", twentyDayRateDetail);

    // 20일 단위 전체 리스트(twentyDayRateDetails)는 보내지 않았음 / 현재 해당하는 20일 단위 출석률+기간(twentyDayRateDetail)
    return Map.of(
        "validAttendanceDays", validAttendanceDays,
        "overallAttendanceRate", overallAttendanceRate,
        "twentyDayRateDetail", twentyDayRateDetail
    );

  }


  /**
   * 주말 및 공휴일, 대체 공휴일을 제외한 유효 출석일 계산
   */

  public static List<LocalDate> getValidDays(LocalDate startDate, LocalDate endDate,
      Set<LocalDate> holidays) {
    log.info("주말 및 공휴일, 대체 공휴일을 제외한 유효 출석일 계산 시작");
    List<LocalDate> validDays = new ArrayList<>();
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      if (!(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
          currentDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
          holidays.contains(currentDate))) {
        validDays.add(currentDate);
      }
      currentDate = currentDate.plusDays(1);
    }

    log.debug("유효한 출석일 수 validDays: {}", validDays);
    log.info("유효 출석일 계산 종료");
    return validDays;
  }


  /**
   * 20일 단위 차수 계산
   */

  public static List<Map<String, Object>> calculateTwentyDaySegments(List<LocalDate> validDays) {
    log.info("20일 단위 차수 계산 시작");
    List<Map<String, Object>> segments = new ArrayList<>();
    int periodIndex = 1;

    while (!validDays.isEmpty()) {
      List<LocalDate> periodDays = validDays.subList(0, Math.min(SEGMENT_DAYS, validDays.size()));
      segments.add(Map.of(
          "차수", periodIndex + "차",
          "시작일", periodDays.get(0),
          "종료일", periodDays.get(periodDays.size()-1),
          "일수", periodDays.size()
      ));
      validDays = validDays.subList(Math.min(SEGMENT_DAYS, validDays.size()), validDays.size());
      periodIndex++;
    }

    log.info("20일 단위 차수 계산 종료");
    log.debug("20일 단위 차수 리스트: {}", segments);
    return segments;
  }


  /**
   * 20일 단위의 기간 설정하는 메소드
   */

  private static List<Map<String, Object>> calculateTwentyDayPeriods(LocalDate startDate,
      LocalDate endDate, Set<LocalDate> holidays) {
    log.info("calculateTwentyDayPeriods - 20일 단위 차수 계산 시작");
    List<Map<String, Object>> twentyDayPeriods = new ArrayList<>();
    LocalDate currentDate = startDate.plusDays(1);
    LocalDate periodStart = null;
    int dayCounter = 0;
    int periodIndex = 1;

    while (!currentDate.isAfter(endDate)) {
      if (!(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY
          || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY || holidays.contains(currentDate))) {
        if (periodStart == null) {
          periodStart = currentDate;
        }
        dayCounter++;

        if (dayCounter == SEGMENT_DAYS) {
          twentyDayPeriods.add(Map.of(
              "periodIndex", periodIndex + "차",
              "startDate", periodStart.toString(),
              "endDate", currentDate.toString()
          ));
          periodStart = null;
          dayCounter = 0;
          periodIndex++;
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    if (periodStart != null) {
      twentyDayPeriods.add(Map.of(
          "periodIndex", periodIndex + "차",
          "startDate", periodStart.toString(),
          "endDate", currentDate.minusDays(1).toString()
      ));
    }
    log.info("twentyDayPeriods - 20일 단위 차수 계산 종료");
    log.debug("twentyDayPeriods - 20일 단위 차수 리스트: {}", twentyDayPeriods);
    return twentyDayPeriods;
  }


  /**
   * 소수점 둘째 자리까지 반올림하는 유틸리티 메서드
   */

  private static double roundToTwoDecimalPlaces(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}





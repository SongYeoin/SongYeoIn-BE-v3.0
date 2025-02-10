package com.syi.project.attendance;

import com.syi.project.attendance.dto.projection.AttendanceDailyStats;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.common.enums.AttendanceStatus;
import java.time.LocalDate;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttendanceCalculator {

  // 출석 상태별 감점 비율 (1교시 기준)
  private static final double LATE_PENALTY = -0.0625;
  private static final double ABSENT_PENALTY = -0.125;

  /**
   * 출석률을 계산하는 메서드
   * @param dailyStats 학생의 출석 데이터 목록
   * @return 전체 출석률과 20일 단위 출석률을 포함하는 맵
   */
  // 115일 전체 출석률 및 20일 단위 출석률 계산
  public static Map<String, Object> calculateAttendanceRates(List<AttendanceDailyStats> dailyStats) {

    log.info("출석률 계산기 로직 진입");

    double totalAttendanceScore = 100.0; // 115일 전체 출석률 (누적)
    List<Double> twentyDayRates = new ArrayList<>(); // 20일 단위 출석률 저장
    double twentyDayScore = 100.0; // 20일 단위 출석률 (리셋됨)

    int dayCount = 0;

    for (var stats : dailyStats) {

      // 하루 동안의 감점 계산
      double dailyPenalty = stats.getLateCount() * LATE_PENALTY +
          stats.getAbsentCount() * ABSENT_PENALTY;

      // 전체 출석률 계산
      totalAttendanceScore += dailyPenalty;
      totalAttendanceScore = Math.max(0, totalAttendanceScore); // 0% 이하 방지

      // 20일 단위 출석률 계산
      twentyDayScore += dailyPenalty;
      twentyDayScore = Math.max(0, twentyDayScore);

      dayCount++;

      // 20일이 지나면 출석률 저장 후 리셋
      if (dayCount % 20 == 0) {
        twentyDayScore = roundToTwoDecimalPlaces(twentyDayScore);
        twentyDayRates.add(twentyDayScore);
        twentyDayScore = 100.0;
      }
    }

    /*// 20일 단위 출석률 평균 계산
    double averageTwentyDayRate = twentyDayRates.stream().mapToDouble(Double::doubleValue).average().orElse(100.0);*/

    // 전체 출석률과 20일 출석률을 소수점 둘째 자리까지 반올림
    totalAttendanceScore = roundToTwoDecimalPlaces(totalAttendanceScore);
    twentyDayScore = roundToTwoDecimalPlaces(twentyDayScore);

    log.debug("전체 출석률(115일) {}", totalAttendanceScore);
    log.debug("20일 출석률(20일) {}", twentyDayScore);

    // 결과 반환
    return Map.of(
        "overallAttendanceRate", totalAttendanceScore,
        "twentyDayRates", twentyDayRates,
        "twentyDayScore", twentyDayScore
    );

  }

  /**
   * 소수점 둘째 자리까지 반올림하는 유틸리티 메서드
   */
  private static double roundToTwoDecimalPlaces(double value) {
    return Math.round(value * 100.0) / 100.0;
  }



}

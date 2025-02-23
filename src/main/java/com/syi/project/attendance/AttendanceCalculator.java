
package com.syi.project.attendance;

import com.syi.project.attendance.dto.projection.AttendanceDailyStats;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AttendanceCalculator {

  // 참여수당 (20일)
  private static final int SEGMENT_DAYS = 20;
  private static final int TOTAL_SESSIONS_PER_DAY = 8; // 하루 8교시
  private static final int THRESHOLD = 3;  // 지각/조퇴 3회시 결석

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

    // 첫 날 제외하고 유효한 전체 출석일 (115일)
    List<LocalDate> validDays = getValidDays(startDate.plusDays(1), endDate, holidays);
    // 20일 단위 차수 계산
    List<Map<String, Object>> segments = calculateTwentyDaySegments(validDays);
    log.debug("20일 단위 차수: {}", segments);

    int totalAttendanceDays = 0; // 학생이 실제 출석한 총 일수
    int totalLateCount = 0;     // 지각한 횟수
    int totalEarlyLeaveCount = 0;   // 조퇴한 횟수

    int accumulatedIncidents = 0; // 전체 기간 동안의 지각+조퇴 누적 횟수

    List<Map<String, Object>> twentyDayRateDetails = new ArrayList<>(); // 20일 단위 출석률 저장 리스트

    int currentSegmentDays = 0;
    int currentSegmentAttendance = 0;
    int currentSegmentLateCount = 0;
    int currentSegmentEarlyLeaveCount = 0;
    int periodIndex = 0;

    int currentSegmentIncidents = 0; // 현재 20일 기간의 지각+조퇴 누적 횟수

    dailyStats.sort(Comparator.comparing(AttendanceDailyStats::getDate)); // 날짜순으로 정렬

    for (AttendanceDailyStats stats : dailyStats) {
      log.info("===============start=================");
      LocalDate date = stats.getDate();
      log.debug("처리 중인 날짜: {}", date);

      // 첫날 제외
      if (date.isEqual(startDate)) {
        continue;
      }

      // 주말 및 공휴일 제외
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
          date.getDayOfWeek() == DayOfWeek.SUNDAY ||
          holidays.contains(date)) {
        continue;
      }

      log.debug("학생 ID: {}",stats.getStudentId() == null ? "학생거라 null" : stats.getStudentId());
      log.debug("totalSessions(출석 데이처가 있는 총 교시 수): {}", stats.getTotalSessions());
      log.debug("lateCount(8교시 중 지각 횟수): {}", stats.getLateCount());
      log.debug("absentCount(8교시 중 결석 횟수): {}", stats.getAbsentCount());
      log.debug("earlyLeaveCount(8교시 중 조퇴 횟수): {}", stats.getEarlyLeaveCount());

      // 출석 인정 여부
      boolean isAbsent = stats.getAbsentCount() == TOTAL_SESSIONS_PER_DAY; // 하루 8교시 전부 결석이면 결석 처리

      // 하루에 하나씩만 카운트
      boolean isLate = !isAbsent && (stats.getAbsentCount() > 0 || stats.getLateCount() > 0);
      boolean isEarlyLeave = !isAbsent && stats.getEarlyLeaveCount() > 0;

      // 전체 출석률용 카운트
      if (!isAbsent) {
        totalAttendanceDays++;
        log.debug("출석일 증가: {}", totalAttendanceDays);

        /*if (isLate) {
          totalLateCount++;
          log.debug("지각 카운트 증가: {}", totalLateCount);
        }

        if (isEarlyLeave) {
          totalEarlyLeaveCount++;
          log.debug("조퇴 카운트 증가: {}", totalEarlyLeaveCount);
        }*/

        if (isLate || isEarlyLeave) {
          accumulatedIncidents++; // 지각이나 조퇴가 있으면 누적

          log.debug("지각+조퇴 합한 값: {}",accumulatedIncidents);
          // 3회가 되면 출석일 하나 차감
          if (accumulatedIncidents >= THRESHOLD) {
            totalAttendanceDays--;
            log.debug("1회 출석일 감소!!");
            accumulatedIncidents -= THRESHOLD; // 3회 초과분만 남김
            log.debug("출석일 감소 후 3회 초과분만 남김: {}", accumulatedIncidents);
          }
        }



/*        int totalAbsenceDays = (totalLateCount / THRESHOLD) +
            (totalEarlyLeaveCount / THRESHOLD);
        totalLateCount %= THRESHOLD;
        totalEarlyLeaveCount %= THRESHOLD;*/



/*        int totalIncidents = (totalLateCount + totalEarlyLeaveCount);
        int totalAbsenceDays = totalIncidents / THRESHOLD;
        totalLateCount %= THRESHOLD;
        totalEarlyLeaveCount %= THRESHOLD;

        if (totalIncidents >= THRESHOLD) {
          totalIncidents %= THRESHOLD;
        }*/

        //log.debug("지각 횟수: {}, 조퇴 횟수: {}", totalLateCount, totalEarlyLeaveCount);
        //log.debug("지각 + 조퇴 총 횟수: {}", accumulatedIncidents);
        //log.debug("차감될 결석일 수: {}", totalAbsenceDays);

        /*// 남은 횟수 계산 (지각과 조퇴 분리)
        int remainingIncidents = totalIncidents % THRESHOLD;
        int previousLateCount = totalLateCount;
        int previousEarlyLeaveCount = totalEarlyLeaveCount;

        // 남은 지각과 조퇴 횟수를 원래 비율대로 유지
        if (totalIncidents > 0) {
          totalLateCount = (previousLateCount * remainingIncidents) / totalIncidents;
          totalEarlyLeaveCount = remainingIncidents - totalLateCount;
        }

        log.debug("지각 + 조퇴 총 횟수: {}", totalIncidents);
        log.debug("이전 지각 횟수: {}, 이전 조퇴 횟수: {}", previousLateCount, previousEarlyLeaveCount);
        log.debug("totalAbsenceDays(결석으로 처리된 일수): {}", totalAbsenceDays);
        log.debug("남은 지각 횟수: {}, 남은 조퇴 횟수: {}", totalLateCount, totalEarlyLeaveCount);
*/

        /*if (totalAbsenceDays > 0) {
          totalAttendanceDays = Math.max(0, totalAttendanceDays - totalAbsenceDays);
          log.debug("결석일 적용한 출석일: {}", totalAttendanceDays);
        }*/


      }

      // 20일 단위 출석률 계산
      currentSegmentDays++;
      if (!isAbsent) {
        currentSegmentAttendance++;

        if (isLate || isEarlyLeave) {
          currentSegmentIncidents++; // 현재 20일 기간의 지각/조퇴 누적

          // 3회가 되면 출석일 하나 차감
          if (currentSegmentIncidents >= THRESHOLD) {
            currentSegmentAttendance--;
            currentSegmentIncidents -= THRESHOLD;
          }
        }

        /*if (isLate) {
          currentSegmentLateCount++;
          log.debug("20일 단위 지각 카운트 증가: {}", currentSegmentLateCount);
        }
        if (isEarlyLeave) {
          currentSegmentEarlyLeaveCount++;
          log.debug("20일 단위 조퇴 카운트 증가: {}", currentSegmentEarlyLeaveCount);
        }

        // 우선 출석일 증가
        currentSegmentAttendance++;
        log.debug("20일 단위 출석일 증가: {}", currentSegmentAttendance);*/

/*        int segmentAbsenceDays = (currentSegmentLateCount / THRESHOLD) +
            (currentSegmentEarlyLeaveCount / THRESHOLD);
        currentSegmentLateCount %= THRESHOLD;
        currentSegmentEarlyLeaveCount %= THRESHOLD;

        if (segmentAbsenceDays > 0) {
          currentSegmentAttendance = Math.max(0, currentSegmentAttendance - segmentAbsenceDays);
        } else {
          currentSegmentAttendance++;
        }*/
      }

      // 매일매일의 20일 단위 출석률 계산
      Map<String, Object> segment = segments.get(periodIndex);
      int daysInSegment = (int) segment.get("일수");  // 현재 차수의 실제 일수

      // 지각과 조퇴의 총 합으로 결석일 계산
      /*int totalSegmentIncidents = currentSegmentLateCount + currentSegmentEarlyLeaveCount;
      int segmentAbsenceDays = totalSegmentIncidents / THRESHOLD;
      currentSegmentAttendance = Math.max(0, currentSegmentAttendance - segmentAbsenceDays);


      if( totalSegmentIncidents >= THRESHOLD) {
        totalSegmentIncidents %= THRESHOLD;
        currentSegmentLateCount %= THRESHOLD;
        currentSegmentEarlyLeaveCount %= THRESHOLD;

      }

      log.debug("20일 단위 지각 횟수: {}, 조퇴 횟수: {}", currentSegmentLateCount, currentSegmentEarlyLeaveCount);
      log.debug("20일 단위 지각+조퇴 총 횟수: {}", totalSegmentIncidents);
      log.debug("20일 단위 차감될 결석일 수: {}", segmentAbsenceDays);
      log.debug("20일 단위 최종 출석일 수: {}", currentSegmentAttendance);*/

      double twentyDayRate = roundToTwoDecimalPlaces(
          (currentSegmentAttendance / (double) daysInSegment) * 100);

      // 이전 데이터가 있다면 업데이트, 없다면 새로 추가
      // 20일 차수 출석률 정보 저장
      Map<String, Object> rateInfo = new HashMap<>();
      rateInfo.put("periodIndex", segment.get("차수"));
      rateInfo.put("startDate", segment.get("시작일").toString());
      rateInfo.put("endDate", segment.get("종료일").toString());
      rateInfo.put("twentyDayRate", twentyDayRate);
      rateInfo.put("currentDay", currentSegmentDays);
      rateInfo.put("incidentCount", currentSegmentIncidents); // 현재 누적된 지각/조퇴 횟수
      rateInfo.put("attendanceDays", currentSegmentAttendance); // 실제 출석일수

      if (twentyDayRateDetails.size() > periodIndex) {
        twentyDayRateDetails.set(periodIndex, rateInfo);
      } else {
        twentyDayRateDetails.add(rateInfo);
      }


      // 20일이 되면 다음 차수로 넘어감
      if (currentSegmentDays == SEGMENT_DAYS) {
        currentSegmentDays = 0;
        currentSegmentAttendance = 0;
        currentSegmentIncidents = 0;  // 지각과 조퇴 초기화
/*        currentSegmentLateCount = 0;
        currentSegmentEarlyLeaveCount = 0;*/
        periodIndex++;
      }

      log.info("================end===============");
    }


    double overallAttendanceRate = roundToTwoDecimalPlaces(
        (totalAttendanceDays / (double) validDays.size()) * 100);


    log.debug("최종 출석 가능일 수: {}", validDays.size());
    log.debug("최종 전체 실제 출석일 수: {}", totalAttendanceDays);
    log.debug("최종 전체 출석률: {}", overallAttendanceRate);
    log.debug("최종 20일 단위 출석률 리스트: {}", twentyDayRateDetails);
    Map<String, Object> twentyDayRateDetail = twentyDayRateDetails.get(
        twentyDayRateDetails.size() - 1);
    log.debug("해당하는 20일 단위 출석률: {}", twentyDayRateDetail);

    return Map.of(
        "validAttendanceDays", validDays.size(),  // 유효일 수
        "overallAttendanceRate", overallAttendanceRate, //  전체 출석률
        "twentyDayRate", twentyDayRateDetails.get(periodIndex).get("twentyDayRate"), // 현재 진행 중인 차수의 출석률
        "twentyDayRates", twentyDayRateDetails  // 모든 차수의 일별 20일 단위 출석률
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
    log.debug("유효한 출석일 수: {}", validDays.size());
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

    // 입력받은 리스트를 새로운 리스트로 복사 (원본 보존)
    List<LocalDate> workingDays = new ArrayList<>(validDays);

    // 현장실습 기간
    // 마지막 20일 제거 (리스트 크기가 20일 이상인 경우에만)
    if (workingDays.size() >= SEGMENT_DAYS) {
      workingDays = workingDays.subList(0, workingDays.size() - SEGMENT_DAYS);
    }

    while (!workingDays.isEmpty()) {
      List<LocalDate> periodDays = workingDays.subList(0, Math.min(SEGMENT_DAYS, workingDays.size()));
      segments.add(Map.of(
          "차수", periodIndex + "차",
          "시작일", periodDays.get(0),
          "종료일", periodDays.get(periodDays.size()-1),
          "일수", periodDays.size()
      ));
      workingDays = workingDays.subList(Math.min(SEGMENT_DAYS, workingDays.size()), workingDays.size());
      periodIndex++;
    }

    log.info("20일 단위 차수 계산 종료");
    log.debug("20일 단위 차수 리스트: {}", segments);
    return segments;
  }



  /**
   * 소수점 둘째 자리까지 반올림하는 유틸리티 메서드
   */

  private static double roundToTwoDecimalPlaces(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}





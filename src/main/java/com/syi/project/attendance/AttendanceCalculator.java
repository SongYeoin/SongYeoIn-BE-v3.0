
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
  public static final int SEGMENT_DAYS = 20;
  public static final int TOTAL_SESSIONS_PER_DAY = 8; // 하루 8교시
  public static final int THRESHOLD = 3;  // 지각/조퇴 3회시 결석

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
    log.info("dailyStats size: {}, startDate: {}, endDate: {}, holidays: {}", dailyStats.size(),
        startDate, endDate, holidays);

    // 첫 날 제외하고 유효한 전체 출석일 (115일)
    List<LocalDate> validDays = getValidDays(startDate, endDate, holidays);

    // validDays가 비어있는 경우 처리
    if (validDays.isEmpty()) {
      log.warn("유효한 출석일이 없습니다. 기본값으로 출석률 정보를 반환합니다.");
      return Map.of(
          "validAttendanceDays", 0,
          "overallAttendanceRate", 0.0,
          "twentyDayRate", 0.0,
          "twentyDayRates", new ArrayList<>()
      );
    }

    // 20일 단위 차수 계산
    List<Map<String, Object>> segments = calculateTwentyDaySegments(validDays);
    //log.debug("20일 단위 차수: {}", segments);

    // dailyStats가 비어있는지 확인 추가
    if (dailyStats == null || dailyStats.isEmpty()) {
      log.warn("dailyStats가 비어 있습니다. 기본값으로 출석률 정보를 반환합니다.");
      return Map.of(
          "validAttendanceDays", validDays.size(),  // 유효일 수
          "overallAttendanceRate", 0.0, //  전체 출석률 기본값
          "twentyDayRate", 0.0, // 현재 진행 중인 차수의 출석률 기본값
          "twentyDayRates", new ArrayList<>()  // 빈 리스트
      );
    }

    dailyStats.sort(Comparator.comparing(AttendanceDailyStats::getDate)); // 날짜순으로 정렬

    // 전체 출석률 계산
    double overallAttendanceRate = calculateOverallAttendanceRate(dailyStats, startDate, validDays);

    // 20일 단위 출석률 계산
    List<Map<String, Object>> twentyDayRateDetails = calculateTwentyDayAttendanceRates(dailyStats, segments);

    // 현재 진행 중인 20일 단위 출석률 가져오기
    double currentTwentyDayRate = 0.0; // 기본값으로 0.0 설정
    if (!twentyDayRateDetails.isEmpty()) {
      Map<String, Object> twentyDayRateDetail = twentyDayRateDetails.get(
          twentyDayRateDetails.size() - 1); // 현재 진행중인 회차 MAP

      // twentyDayRate가 null인지 확인
      if (twentyDayRateDetail.containsKey("twentyDayRate")) {
        currentTwentyDayRate = (double) twentyDayRateDetail.get("twentyDayRate");
      } else {
        log.warn("twentyDayRateDetail에 twentyDayRate 키가 없습니다.");
      }
    } else {
      log.warn("twentyDayRateDetails가 비어 있습니다. 기본값(0.0)을 사용합니다.");
    }

    log.info("(최종)출석 가능일 수: {}, 전체 출석률: {}, 20일 단위 출석률 리스트: {}, 해당 20일 단위 출석률: {}",
        validDays.size(), overallAttendanceRate, twentyDayRateDetails, currentTwentyDayRate);
    log.debug("최종 출석 가능일 수: {}", validDays.size());
    log.debug("최종 전체 출석률:{}",overallAttendanceRate);
    log.debug("최종 20일 단위 출석률 리스트: {}", twentyDayRateDetails);
    log.debug("해당하는 20일 단위 출석률: {}", currentTwentyDayRate);

    return Map.of(
        "validAttendanceDays", validDays.size(),  // 유효일 수
        "overallAttendanceRate", overallAttendanceRate, //  전체 출석률
        "twentyDayRate", currentTwentyDayRate, // 현재 진행 중인 차수의 출석률
        "twentyDayRates", twentyDayRateDetails  // 모든 차수의 일별 20일 단위 출석률
    );

  }

  /**
   * 전체 출석률 구하는 메소드
   */
  public static double calculateOverallAttendanceRate(List<AttendanceDailyStats> dailyStats,
      LocalDate startDate,
      List<LocalDate> validDays){

    // 유효일 수가 0이거나 1인 경우
    if (validDays.size() <= 1) {
      log.warn("유효 출석일이 부족합니다. 출석률을 0.0으로 반환합니다.");
      return 0.0;
    }

    // dailyStats가 비어있는 경우
    if (dailyStats == null || dailyStats.isEmpty()) {
      log.warn("출석 데이터가 없습니다. 출석률을 0.0으로 반환합니다.");
      return 0.0;
    }



    int totalAttendanceDays = 0;
    int accumulatedIncidents = 0; // 전체 기간 동안 지각+조퇴 누적 횟수

    for (AttendanceDailyStats stats : dailyStats) {
      log.debug("(전체 출석률)stats: {}",stats.toString());

      LocalDate date = stats.getDate();

      // 유효한 출석일만 고려
      if (!validDays.contains(date) || date.isEqual(startDate)) {
        continue;
      }


      int totalSessions = stats.getTotalSessions();
      if (totalSessions == 0) {
        log.warn("날짜 {}의 총 교시 수가 0입니다. 8교시로 간주합니다.", date);
        totalSessions = TOTAL_SESSIONS_PER_DAY;
      }


      log.info("해당 날짜: {}, totalSessions: {}, lateCount: {}, absentCount: {}, earlyLeaveCount: {}",
          date, stats.getTotalSessions(), stats.getLateCount(), stats.getAbsentCount(),
          stats.getEarlyLeaveCount());
      log.debug("학생 ID: {}",stats.getStudentId() == null ? "학생거라 null" : stats.getStudentId());
      log.debug("totalSessions(출석 데이처가 있는 총 교시 수): {}", stats.getTotalSessions());
      log.debug("lateCount(8교시 중 지각 횟수): {}", stats.getLateCount());
      log.debug("absentCount(8교시 중 결석 횟수): {}", stats.getAbsentCount());
      log.debug("earlyLeaveCount(8교시 중 조퇴 횟수): {}", stats.getEarlyLeaveCount());

      boolean isAbsent = totalSessions > 0 && stats.getAbsentCount() == TOTAL_SESSIONS_PER_DAY;
      boolean isLate = !isAbsent && (stats.getAbsentCount() > 0 || stats.getLateCount() > 0);
      boolean isEarlyLeave = !isAbsent && stats.getEarlyLeaveCount() > 0;

      if (!isAbsent) {
        totalAttendanceDays++;
        log.info("출석일 증가: {}", totalAttendanceDays);


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
      }
      log.info("지각과 조퇴 합한 값 근황: {}, 실제 출석일 근황: {}", accumulatedIncidents, totalAttendanceDays);
    }

    log.debug("최종 전체 실제 출석일 수: {}", totalAttendanceDays);
    double realValidDaysSize = (double) validDays.size() - 1;
    if (realValidDaysSize <= 0) {
      log.warn("유효 출석일 수가 1 이하입니다. 출석률을 0.0으로 반환합니다.");
      return 0.0;
    }
    log.debug("(double) validDays.size()) - 1: {}",realValidDaysSize);

    log.info("(최종) 전체 실제 출석일 수: {}, 1일 감소된 유효일 수: {}", totalAttendanceDays, realValidDaysSize);
    return roundToTwoDecimalPlaces((totalAttendanceDays / realValidDaysSize) * 100);
  }

  /**
   * 20일 단위 출석률 구하는 메소드
   */
  private static List<Map<String, Object>> calculateTwentyDayAttendanceRates(List<AttendanceDailyStats> dailyStats,
      List<Map<String, Object>> segments) {

    log.info("20일 단위 출석률 구하기 요청");
    List<Map<String, Object>> twentyDayRateDetails = new ArrayList<>();
    int periodIndex = 0;
    int currentSegmentDays = 0;
    int currentSegmentAttendance = 0;
    int currentSegmentIncidents = 0;

    // dailyStats가 비어있는지 추가 확인
    if (dailyStats == null || dailyStats.isEmpty()) {
      log.warn("dailyStats가 비어 있어서 빈 리스트를 반환합니다.");
      return twentyDayRateDetails;
    }

    // dailyStats에서 마지막 날짜 찾기
    LocalDate lastRecordedDate = dailyStats.stream()
        .map(AttendanceDailyStats::getDate)
        .max(LocalDate::compareTo)
        .orElse(null);

    if (lastRecordedDate == null) {
      log.warn("dailyStats에 데이터가 없어서 빈 리스트를 반환합니다.");
      return twentyDayRateDetails; // 데이터가 없으면 빈 리스트 반환
    }

    // segments가 비어있는지 확인
    if (segments == null || segments.isEmpty()) {
      log.warn("segments가 비어 있어서 빈 리스트를 반환합니다.");
      return twentyDayRateDetails;
    }

    // 마지막 날짜가 포함된 세그먼트 찾기
    boolean processedLastSegment = false;

    for (Map<String, Object> segment : segments) {

      List<LocalDate> periodDays = new ArrayList<>();
      Object rawObject = segment.get("날짜들");

      if (rawObject instanceof List<?>) {
        periodDays = ((List<?>) rawObject).stream()
            .filter(LocalDate.class::isInstance) // LocalDate 타입인지 확인
            .map(LocalDate.class::cast) // 안전한 형변환
            .toList();
      }


/*      // 첫 번째 차수(1차)만 첫째 날을 제외
      if (periodIndex == 0 && periodDays.size() > 1) {
        periodDays = periodDays.subList(1, periodDays.size()); // 첫째 날 제외
      }*/

      // 이 세그먼트가 마지막 기록된 날짜를 포함하는지 확인
      boolean containsLastDate = periodDays.stream()
          .anyMatch(date -> date.isEqual(lastRecordedDate) || date.isAfter(lastRecordedDate));

      List<LocalDate> calculationDays = periodDays;
      log.info("calculationDays 의 크기: {}", calculationDays.size());


      currentSegmentDays = 0;
      currentSegmentAttendance = 0;
      currentSegmentIncidents = 0;

      for (LocalDate date : calculationDays) {

        // 마지막 기록된 날짜 이후의 날짜는 건너뜀
        if (date.isAfter(lastRecordedDate)) {
          continue;
        }

        AttendanceDailyStats stats = dailyStats.stream()
            .filter(s -> s.getDate().isEqual(date))
            .findFirst()
            .orElse(null);

        if (stats == null) {
          log.info("출석 데이터가 없어서 건너뜁니다.");
          continue;
        } // 출석 데이터가 없으면 건너뜀

        boolean isAbsent = stats.getAbsentCount() == TOTAL_SESSIONS_PER_DAY;
        boolean isLate = !isAbsent && (stats.getAbsentCount() > 0 || stats.getLateCount() > 0);
        boolean isEarlyLeave = !isAbsent && stats.getEarlyLeaveCount() > 0;

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
        }
        log.info("소정출석일 근황: {},실제 출석일 근황: {}, 축적된 지각과 조퇴합 근황: {}",
            currentSegmentDays, currentSegmentAttendance, currentSegmentIncidents);
      }

      if (currentSegmentDays > 0) {
        log.debug("currentSegmentDays-20일(소정출석일): {}",currentSegmentDays);

        double twentyDayRate = roundToTwoDecimalPlaces((currentSegmentAttendance / (double) currentSegmentDays) * 100);
        log.info("twentyDayRate: {}", twentyDayRate);

        // 차수별 출석률 정보 저장
        Map<String, Object> rateInfo = new HashMap<>();
        rateInfo.put("periodIndex", segment.get("차수"));
        rateInfo.put("startDate", segment.get("시작일").toString());
        rateInfo.put("endDate", segment.get("종료일").toString());
        rateInfo.put("twentyDayRate", twentyDayRate);
        rateInfo.put("currentDay", currentSegmentDays);
        rateInfo.put("incidentCount", currentSegmentIncidents);
        rateInfo.put("attendanceDays", currentSegmentAttendance);
        log.info("차수별 정보 저장: {}", rateInfo);

        if (twentyDayRateDetails.size() > periodIndex) {
          twentyDayRateDetails.set(periodIndex, rateInfo);
        } else {
          twentyDayRateDetails.add(rateInfo);
        }
      }

      // 이 세그먼트가 마지막 기록 날짜를 포함하면 여기서 종료
      if (containsLastDate) {
        log.info("stats의 마지막 날짜이므로 20일 출석률 계산 과정을 종료합니다.");
        processedLastSegment = true;
        break;
      }

      periodIndex++;
    }

    log.debug("(20일 출석률)최종 20일 단위 출석률 리스트: {}", twentyDayRateDetails);
    // 리스트가 비어있는지 확인하는 방어 코드 추가
    if (!twentyDayRateDetails.isEmpty()) {
      Map<String, Object> twentyDayRateDetail = twentyDayRateDetails.get(
          twentyDayRateDetails.size() - 1);
      log.debug("(20일 출석률)해당하는 20일 단위 출석률: {}", twentyDayRateDetail);
      log.info("(최종)20일 단위 출석률 리스트: {}, 해당하는 20일 단위 출석률: {}", twentyDayRateDetails,
          twentyDayRateDetail);
    } else {
      log.warn("20일 단위 출석률 계산 결과가 없습니다.");
    }
    return twentyDayRateDetails;
  }

  /**
   * 프린트할 때 사용할 20일 계산 메소드
   */
  public static Map<String,Object> calculateTwentyDayAttendanceRatesForPrint(List<AttendanceDailyStats> dailyStats,List<LocalDate> courseDates){
    Map<String, Object> twentyDayRateDetails = new HashMap<>();

    log.info("프린트를 위해 20일 출석률 계산을 시작합니다.");

    int currentSegmentDays = 0;
    int currentSegmentAttendance = 0;
    int currentSegmentIncidents = 0;
    int absent = 0;
    int late = 0;
    int earlyLeave = 0;

    for (LocalDate date : courseDates) {
      AttendanceDailyStats stats = dailyStats.stream()
          .filter(s -> s.getDate().isEqual(date))
          .findFirst()
          .orElse(null);

      if (stats == null) {
        log.info("(프린트) 출석 데이터가 없어서 건너뜁니다.");
        continue;
      } // 출석 데이터가 없으면 건너뜀


      boolean isAbsent = stats.getAbsentCount() == TOTAL_SESSIONS_PER_DAY;
      boolean isLate = !isAbsent && (stats.getAbsentCount() > 0 || stats.getLateCount() > 0);
      boolean isEarlyLeave = !isAbsent && stats.getEarlyLeaveCount() > 0;

      currentSegmentDays++;
      if (!isAbsent) {
        currentSegmentAttendance++;

        if (isLate || isEarlyLeave) {
          currentSegmentIncidents++; // 현재 20일 기간의 지각/조퇴 누적

          if(isLate){
            late++;
          }else {
            earlyLeave++;
          }

          // 3회가 되면 출석일 하나 차감
          if (currentSegmentIncidents >= THRESHOLD) {
            currentSegmentAttendance--;
            currentSegmentIncidents -= THRESHOLD;
          }
        }
      }else{
        absent++;
      }

      log.info("(프린트) 결석: {}, 지각: {}, 조퇴: {}",absent,late,earlyLeave);
      log.info("(프린트) 소정출석일 근황: {},실제 출석일 근황: {}, 축적된 지각과 조퇴합 근황: {}",
          currentSegmentDays, currentSegmentAttendance, currentSegmentIncidents);
    }

    log.info("(프린트)소정 출석일: {}",currentSegmentDays);
    log.info("(프린트)실제 출석일: {}", currentSegmentAttendance);
    double twentyDayRate = roundToTwoDecimalPlaces((currentSegmentAttendance / (double) currentSegmentDays) * 100);
    log.info("(프린트)20일 출석률: {}", twentyDayRate);
    log.info("(프린트)결석 횟수: {}",absent);
    log.info("(프린트)지각 횟수: {}",late);
    log.info("(프린트)조퇴 횟수: {}",earlyLeave);

    twentyDayRateDetails.put("totalWorkingDays",currentSegmentDays);
    twentyDayRateDetails.put("attendanceDays",currentSegmentAttendance);
    twentyDayRateDetails.put("lateDays",late);
    twentyDayRateDetails.put("earlyLeaveDays",earlyLeave);
    twentyDayRateDetails.put("absentDays",absent);
    twentyDayRateDetails.put("attendanceRate",twentyDayRate);
    log.info("(프린트-최종) 20일 출석룰 Map: {}",twentyDayRateDetails);

    return twentyDayRateDetails;

  }



  /**
   * 주말 및 공휴일, 대체 공휴일을 제외한 유효 출석일 계산
   */

  public static List<LocalDate> getValidDays(LocalDate startDate, LocalDate endDate,
      Set<LocalDate> holidays) {
    log.info("주말 및 공휴일, 대체 공휴일을 제외한 유효 출석일 계산 시작");
    log.info("startDate: {}, endDate: {}, holidays: {}", startDate, endDate, holidays);
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
    log.info("유효한 출석일 수: {}", validDays);
    log.info("유효 출석일 계산 종료");
    return validDays;
  }


  /**
   * 20일 단위 차수 계산
   */

  public static List<Map<String, Object>> calculateTwentyDaySegments(List<LocalDate> validDays) {
    log.info("20일 단위로 차수를 나누는 계산 시작");
    List<Map<String, Object>> segments = new ArrayList<>();
    int periodIndex = 1;

    // 입력받은 리스트를 새로운 리스트로 복사 (원본 보존)
    List<LocalDate> workingDays = new ArrayList<>(validDays);

    // 현장실습 기간
    // 마지막 20일 제거 (리스트 크기가 20일 이상인 경우에만)
    if (workingDays.size() >= SEGMENT_DAYS) {
      log.info("현장실습 날짜를 제외합니다 (20일)");
      workingDays = workingDays.subList(0, workingDays.size() - SEGMENT_DAYS);
    }

    while (!workingDays.isEmpty()) {
      int daysInSegment = SEGMENT_DAYS;
      log.info("이번 차수의 크기: {}",daysInSegment);
      int segmentSize = Math.min(daysInSegment, workingDays.size());

      List<LocalDate> periodDays = workingDays.subList(0, segmentSize);
//      List<LocalDate> periodDays = workingDays.subList(0, Math.min(daysInSegment, workingDays.size()));
      segments.add(Map.of(
          "차수", periodIndex + "차",
          "시작일", periodDays.get(0),
          "종료일", periodDays.get(periodDays.size()-1),
          "일수", periodDays.size(),
          "날짜들", periodDays,
          "첫차여부", periodIndex == 1
      ));
//      workingDays = workingDays.subList(Math.min(SEGMENT_DAYS, workingDays.size()), workingDays.size());
      workingDays = workingDays.subList(segmentSize, workingDays.size());
      periodIndex++;
    }

    log.info("20일 단위 차수 리스트: {}", segments);
    log.info("20일 단위 차수 계산 종료");
    return segments;
  }



  /**
   * 소수점 둘째 자리까지 반올림하는 유틸리티 메서드
   */

  private static double roundToTwoDecimalPlaces(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}





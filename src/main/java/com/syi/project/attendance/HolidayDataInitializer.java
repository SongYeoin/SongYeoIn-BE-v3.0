package com.syi.project.attendance;

import com.syi.project.attendance.service.HolidayService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayDataInitializer {

  private final HolidayService holidayService;

  @PostConstruct
  public void init() {
    try {
      int currentYear = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
          .atZone(ZoneId.systemDefault()).toLocalDate().getYear();
      int nextYear = currentYear + 1;

      log.info("🚀 공휴일 데이터 초기화 시작 - 저장할 연도: {}, {}", currentYear, nextYear);

      // ✅ 최초 실행 시 현재 연도 및 다음 연도의 공휴일 데이터 저장
      holidayService.fetchAndStoreHolidays(currentYear);
      holidayService.fetchAndStoreHolidays(nextYear);

      log.info("✅ 공휴일 데이터 초기화 완료");
    } catch (Exception e) {
      log.error("❌ 공휴일 데이터 초기화 중 오류 발생: ", e);
    }
  }
}

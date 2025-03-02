package com.syi.project.attendance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syi.project.attendance.entity.Holiday;
import com.syi.project.attendance.repository.HolidayRepository;
import com.syi.project.common.enums.HolidayType;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.web.util.UriBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayService {

  private final WebClient webClient;
  private final HolidayRepository holidayRepository;

  @Value("${public.api.service-key}")
  private String serviceKey;

  // 공공데이터 API에서 특정 연도의 공휴일을 가져와 DB에 저장
  @Transactional
  public void fetchAndStoreHolidays(int year) {

    // 먼저 해당 연도의 데이터가 이미 존재하는지 확인
    if (isHolidayDataExistsForYear(year)) {
      log.info("📢 {}년 공휴일 데이터가 이미 충분히 존재합니다. API 호출을 건너뜁니다.", year);
      return; // API 호출 없이 종료
    }

    log.info("📢 {}년 공휴일 데이터를 불러오는 중...", year);


    // 1. 법정 공휴일 조회
    List<Holiday> holidays = fetchHolidaysFromApi(year, "getHoliDeInfo");

    // 2. 대체 공휴일 조회
    List<Holiday> substituteHolidays = fetchHolidaysFromApi(year, "getRestDeInfo");

    log.info("📌 {}년 공휴일 개수: {}", year, holidays.size());
    log.info("📌 {}년 대체공휴일 개수: {}", year, substituteHolidays.size());

    List<Holiday> allHolidays = Stream.concat(holidays.stream(), substituteHolidays.stream())
        .toList();

    log.info("📝 {}년 총 {}개의 공휴일을 저장 시도 중...", year, allHolidays.size());

    // ✅ 개별적으로 DB에서 중복 확인 후 저장
    for (Holiday holiday : allHolidays) {
      if (!holidayRepository.existsByDate(holiday.getDate())) {
        holidayRepository.save(holiday);
        log.info("✅ 저장 완료: {} ({})", holiday.getDate(), holiday.getName());
      } else {
        log.info("⚠️ 중복 데이터 스킵: {} ({})", holiday.getDate(), holiday.getName());
      }
    }

    log.info("✅ {}년 공휴일 데이터 저장 완료!", year);
  }

  // API 호출 후 DB 저장
  private List<Holiday> fetchHolidaysFromApi(int year, String apiType) {

    String API_URL = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/" + apiType;


    // ✅ URI를 직접 문자열로 만들기
    String url = API_URL + "?ServiceKey=" + serviceKey + "&solYear=" + year + "&_type=json";

    log.info("📢 url API 요청: {}", url);


    String response = webClient.get()
        .uri(URI.create(url)) // ✅ `URI.create(url)`을 사용하여 추가 인코딩 방지
        .retrieve()
        .bodyToMono(String.class)
        .block();

    log.info("📌 API 응답: {}", response); // 👈 응답 확인!

    return parseResponse(response);
  }

  // JSON 응답을 Holiday 객체 리스트로 변환
  private List<Holiday> parseResponse(String json) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode root = objectMapper.readTree(json);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      if (items.isMissingNode()) { // 👈 `items`가 없을 경우
        log.warn("⚠️ 공공데이터 API에서 공휴일 데이터가 없음! 응답 확인 필요.");
        return new ArrayList<>(); // 빈 리스트 반환
      }

      List<Holiday> holidays = new ArrayList<>();
      if (items.isArray()) {
        for (JsonNode item : items) {
          LocalDate date = LocalDate.parse(item.get("locdate").asText(),
              DateTimeFormatter.ofPattern("yyyyMMdd"));
          String name = item.get("dateName").asText();

          // HolidayType 결정 (대체공휴일 포함 여부 체크)
          HolidayType type;
          if (name.contains("대체공휴일")) {
            type = HolidayType.SUBSTITUTE_HOLIDAY;
          } else {
            type = HolidayType.HOLIDAY;
          }

          // Holiday 객체 생성 후 리스트에 추가
          holidays.add(new Holiday(date, name, type));
        }
      }
      return holidays;
    } catch (Exception e) {
      throw new RuntimeException("공공데이터 API 응답 파싱 실패", e);
    }
  }

  // 특정 날짜가 공휴일인지 확인
  public String checkHoliday(LocalDate date) {
    return holidayRepository.findByDate(date)
        .map(Holiday::getType)
        .map(Enum::name)
        .orElse("평일");
  }

  // ✅ 특정 연도의 공휴일 데이터를 DB에서 가져오기
  public Set<LocalDate> getHolidaysForYear(int year) {
    return holidayRepository.findAll().stream()
        .map(Holiday::getDate) // 해당 연도의 공휴일만 필터링
        .filter(date -> date.getYear() == year)
        .collect(Collectors.toSet());
  }

  // 매년 12월 31일 자동 업데이트 (스케줄러 실행)
  @Scheduled(cron = "0 0 0 31 12 ?") // 매년 12월 31일 00:00 실행
  public void scheduledHolidayUpdate() {
    fetchAndStoreHolidays(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
        .atZone(java.time.ZoneId.systemDefault()).toLocalDate().getYear() + 1);
  }

  // 특정 연도의 공휴일 데이터가 DB에 충분히 존재하는지 확인
  public boolean isHolidayDataExistsForYear(int year) {
    // 해당 연도의 공휴일 개수를 조회
    long count = holidayRepository.countByDateBetween(
        LocalDate.of(year, 1, 1),
        LocalDate.of(year, 12, 31)
    );

    // 일정 개수 이상이면 데이터가 충분히 있다고 판단 (예: 최소 10개 이상)
    return count >= 5; // 한국 공휴일은 보통 15개 내외이므로 10개 정도면 충분히 있다고 판단
  }
}

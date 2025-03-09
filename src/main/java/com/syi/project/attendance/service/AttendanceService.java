package com.syi.project.attendance.service;

import static com.syi.project.attendance.AttendanceCalculator.TOTAL_SESSIONS_PER_DAY;
import static com.syi.project.attendance.AttendanceCalculator.calculateTwentyDayAttendanceRatesForPrint;
import static com.syi.project.attendance.AttendanceCalculator.calculateTwentyDaySegments;
import static com.syi.project.attendance.AttendanceCalculator.getValidDays;
import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ALREADY_ENTERED;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ALREADY_EXITED;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ENTRY_INVALID_DATE;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ENTRY_NOT_ALLOWED;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ENTRY_NOT_FOUND;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ENTRY_TOO_EARLY;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_ENTRY_TOO_LATE;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_EXIT_NOT_ALLOWED;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_EXIT_NOT_FIND_PERIOD;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_FAILED;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_NOT_IN_RANGE;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_SEGMENT_NOT_FOUND;
import static com.syi.project.common.exception.ErrorCode.COURSE_NOT_FOUND;
import static com.syi.project.period.eneity.QPeriod.period;

import com.querydsl.core.Tuple;
import com.syi.project.attendance.AttendanceCalculator;
import com.syi.project.attendance.dto.AttendanceDTO;
import com.syi.project.attendance.dto.projection.AttendanceDailyStats;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.StudentAllAttendRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendancePrintResponseDto;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendancePrintResponseDto.SummaryPageDto;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceStatusListDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceTableDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.MemberInfoInDetail;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.period.dto.PeriodResponseDTO;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.repository.ScheduleRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;
  private final CourseRepository courseRepository;
  private final ScheduleRepository scheduleRepository;
  //private final EnrollRepository enrollRepository;
  private final PeriodRepository periodRepository;
  private final EnrollRepository enrollRepository;
  private final HolidayService holidayService;

  // 담당자
  /* 출석 전체 조회 */
  public Page<AttendListResponseDTO> getAllAttendancesForAdmin(CustomUserDetails userDetails,
      Long courseId, AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable) {
    /* 담당자는 courseId, studentId, date, member_name, attendance_status */

    log.info("관리자 전체 출석 조회합니다.");
    Long adminId = userDetails.getId();
    log.info("관리자 Id: {}, courseId: {}, date: {} filter(studentName={}, status ={})", adminId,courseId, dto.getDate(),dto.getStudentName(), dto.getStatus());
    log.debug("필터링 조건 : studentName={}, status ={}", dto.getStudentName(), dto.getStatus());

    // 1교시, 2교시... 교시명 모음
    // 해당 날짜의 요일 (한국어로)
    String dayOfWeek = dto.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN)+"요일";
    log.info("해당하는 요일: {}",dayOfWeek);

    List<String> periods = periodRepository.findPeriodsByDayOfWeek(courseId, dayOfWeek);
    log.info("{} 의"
        + " 교시명 모음: {}",dayOfWeek, periods);

    return attendanceRepository.findPagedAttendListByCourseId(courseId, dto, periods,
        pageable);

  }


  // 수강생
  // 출석 전체 조회
  public Page<AttendListResponseDTO> getAllAttendancesForStudent(CustomUserDetails userDetails,
      Long courseId, StudentAllAttendRequestDTO dto, Pageable pageable) {

    log.info("수강생 전체 출석 조회합니다.");
    Long studentId = userDetails.getId();
    log.info("수강생ID: {},courseId: {}, startDate: {}, endDate: {}, filter(status ={})",studentId, courseId, dto.getStartDate(),
        dto.getEndDate(),dto.getStatus());
    log.debug("필터링 조건 : status ={}", dto.getStatus());

    // 1교시, 2교시... 교시명 모음
    List<String> periods = List.of("1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시", "8교시");

    /*List<String> periods = periodRepository.findPeriodsInRange(courseId,dto.getStartDate(),
        dto.getEndDate());*/
    log.debug("조회된 교시 모음: {}", periods);
    log.info("조회된 교시 사이즈: {}",periods.size());


    AllAttendancesRequestDTO requestDTO = AllAttendancesRequestDTO.builder()
        .date(null)
        .studentId(studentId)
        .startDate(dto.getStartDate())
        .endDate(dto.getEndDate())
        .status(dto.getStatus())
        .build();
    log.info("dto 변환 {}",requestDTO);

    Page<AttendListResponseDTO> responseDTOS = attendanceRepository.findPagedAttendListByCourseId(courseId, requestDTO, periods,
        pageable);
    log.info("수강생 전체 출석 조회 완료 responseDTOS: {}",responseDTOS);


    return responseDTOS;

  }

  //  관리자
  /* 학생별 출석 조회 => 출석 전체 조회랑 같이 만들어질 확률 많음 */
  /*public AttendanceResponseDTO getAttendanceByCourseIdAndMemberId(AttendanceRequestDTO dto) {
    List<Attendance> results = attendanceRepository.findAllAttendance()
    return null;
  }*/

  /* 교시번호와 수강생 번호로 단일 출석 조회하기 */
//  public AttendanceResponseDTO getAttendanceByPeriodAndMember(AttendanceRequestDTO dto) {
//    log.info("PeriodID와 MemberID 로 단일 출석 조회를 시도합니다.");
//    log.debug("단일 출석 조회 요청된 정보: {}", dto);
//
//    log.info("요청된 정보로 출석 조회");
//    /*List<Attendance> results = attendanceRepository.findAllAttendance(dto);
//
//    if (results.isEmpty()) {
//      log.warn("경고 : PeriodID {} 와 MemberID {} 로 조회한 결과가 없습니다.", dto.getPeriodId(),
//          dto.getMemberId());
//      throw new NoSuchElementException("조회된 출석이 없습니다.");
//    }
//
//    log.info("{} 개의 시간표 조회 중", results.size());
//
//    // 조회한 결과 dto로 변환
//    return fromEntity(results.get(0));*/
//    return null;
//
//  }

  //  관리자
  /* 출석 수정 */
  @Transactional
  public AttendanceResponseDTO updateAttendance(Long attendanceId, String status) {
    /* attendanceId status */
    log.info("{}에 대한 출석 상태를 수정합니다.", attendanceId);
    log.info("출석 상태 수정 요청된 정보: {}", status);

    Attendance attendance = attendanceRepository.findById(attendanceId)
        .orElseThrow(() -> {
          log.error("에러: 출석 ID {} 에 대한 출석을 찾을 수 없습니다.", attendanceId);
          return new NoSuchElementException("Attendance not found with id " + attendanceId);
        });
    log.info("출석 ID {}을 찾았습니다.", attendanceId);
    AttendanceStatus newStatus = AttendanceStatus.fromENStatus(status); // 한글을 Enum으로 변환
    attendance.updateStatus(newStatus);
    //attendance.updateModifiedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    Attendance saved = attendanceRepository.save(attendance);
    AttendanceResponseDTO savedStatus = AttendanceResponseDTO.builder()
        .attendanceId(saved.getId())
        .status(saved.getStatus().toKorean())
        .build();

    log.info("{}에 대한 출석 상태를 성공적으로 수정했습니다.", savedStatus);
    return savedStatus;
  }

  //  관리자
  /* 결석 처리 */
  /*@Transactional
  public void updateAbsentStatus(AttendanceRequestDTO dto) {

    log.info(" 출석 상태를 결석으로 수정합니다.");

    // 어제 날짜
    LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
    log.debug("어제의 날짜: {}", yesterday);

    // 어제의 요일 확인
    String dayOfWeekString = convertDayOfWeekToString(yesterday.getDayOfWeek());
    log.debug("어제의 요일을 String 형태로 변환: {}", dayOfWeekString);

    // 해당 요일의 수업(교시) 리스트 조회
    List<Period> periodList = periodRepository.getScheduleByDayOfWeek(dayOfWeekString);

    if (periodList.isEmpty()) {
      log.warn("경고 : dayOfWeekString {}에 대한 교시가 비어있습니다.", dayOfWeekString);
      throw new NoSuchElementException("교시가 비어있습니다.");
    }
    log.info("{} 개의 교시 조회", periodList.size());

    // 반 별 교시 번호 맵 생성 <반 번호, 교시 리스트>
    Map<Long, List<Long>> periodIdListWithCourseIdMap = periodList.stream()
        .collect(Collectors.groupingBy(
            Period::getCourseId,
            Collectors.mapping(Period::getId, Collectors.toList())
        ));

    // 반 별 수강생 리스트 조회 <반 번호, 수강생 리스트>
    Map<Long, List<Member>> studentWithCourseMap = periodIdListWithCourseIdMap.keySet().stream()
        .collect(Collectors.toMap(
            courseId -> courseId,
            enrollRepository::findStudentByCourseId,
            (existing, replacement) -> existing // 중복 키 처리
        ));

    // 특정 날짜에 출석 정보가 없는 학생 처리
    studentWithCourseMap.forEach((courseId, students) -> {
      List<Long> periodIds = periodIdListWithCourseIdMap.get(courseId);
      students.forEach(
          student -> processStudentAttendance(yesterday, courseId, student, periodIds));
    });

    log.info("{}에 대한 출석 상태를 결석으로 수정합니다.", dto.getAttendanceIds());

    for (Long id : dto.getAttendanceIds()) {
      Attendance attendance = attendanceRepository.findById(id)
          .orElseThrow(() -> {
            log.error("에러: ID {} 에 대한 출석을 찾을 수 없습니다.", id);
            return new NoSuchElementException("Attendance not found with id " + id);
          });
      attendance.updateStatus(AttendanceStatus.ABSENT);
      attendanceRepository.save(attendance);
    }

    log.info("성공적으로 {}에 대한 출석 상태를 결석으로 수정했습니다.", dto.getAttendanceIds());
  }*/

  @Transactional
  protected void processStudentAttendance(LocalDate yesterday, Long courseId, Member student,
      List<Long> periodIds) {
    // 특정 학생의 해당 날짜 출석 정보 조회
    List<Attendance> attendanceList = attendanceRepository.findAttendanceByDateAndMemberId(
        yesterday, student.getId());

    // 출석이 없으면 모든 교시에 대해 결석 처리
    if (attendanceList.isEmpty()) {
      periodIds.forEach(periodId -> enrollAbsentAttendance(yesterday, courseId, student, periodId));
    } else {
      // 없는 교시만 추출
      List<Long> attendedPeriodIds = attendanceList.stream()
          .map(Attendance::getPeriodId)
          .toList();
      List<Long> absentPeriodIds = attendedPeriodIds.stream()
          .filter(periodId -> !attendedPeriodIds.contains(periodId))
          .toList();

      // 결석 처리
      absentPeriodIds.forEach(
          periodId -> enrollAbsentAttendance(yesterday, courseId, student, periodId));

    }
  }

  @Transactional
  protected void enrollAbsentAttendance(LocalDate yesterday, Long courseId, Member student,
      Long periodId) {
    /* 교시당 출석은 하나만 존재하므로 periodId로 검증 */
    log.info("결석 처리 하는 메소드 진입 (enrollAbsentAttendance)");
    log.info("yesterday={}, courseId={}, studentId={}, periodId={}",yesterday,courseId,student.getId(),periodId);
    Attendance attendance = new Attendance(null, AttendanceStatus.ABSENT, yesterday, null, null,
        periodId, courseId,
        student.getId(), null, null, null);
    log.info("결석상태로 저장하려는 출석의 정보: attendance={}",attendance);
    Attendance savedAttendance = attendanceRepository.save(attendance);
    log.info("성공적으로 결석 상태를 처리했습니다.");
    log.info("결석 처리한 출석의 정보: savedAttendance={}",savedAttendance);
  }

  private String convertDayOfWeekToString(DayOfWeek dayOfWeek) {
    log.info("convertDayOfWeekToString");
    log.debug("dayOfWeek: {} ", dayOfWeek);
    return switch (dayOfWeek) {
      case MONDAY -> "월요일";
      case TUESDAY -> "화요일";
      case WEDNESDAY -> "수요일";
      case THURSDAY -> "목요일";
      case FRIDAY -> "금요일";
      case SATURDAY -> "토요일";
      case SUNDAY -> "일요일";
    };
  }

  //  수강생
  /* 출석 등록 */
  @Transactional
  public AttendanceResponseDTO createAttendance(CustomUserDetails userDetails, Long courseId,
      String attendanceType, Long earlyLeavePeriodId,
      HttpServletRequest request) {
    log.info("출석 체크 시도 (입실/퇴실/조퇴 여부: {})", attendanceType); /* ENTER면 입실, EARLY_EXIT면 조퇴, EXIT면 퇴실 */

    LocalDateTime now = LocalDateTime.now();

    int year = now.getYear(); // 교육과정의 연도를 기준으로 공휴일 가져오기

    log.info("now: {}, dayOfWeek: {}, 오늘의 연도: {}", now, now.getDayOfWeek()
        .getDisplayName(TextStyle.FULL, Locale.KOREAN),year);


    // ✅ 해당 연도의 공휴일 정보를 DB에서 가져오기
    Set<LocalDate> holidays = holidayService.getHolidaysForYear(year);


    /*
    * holidays나 주말일 경우 입실/퇴실이 불가능하게 예외처리
    * */
    LocalDate today = now.toLocalDate(); // LocalDateTime에서 LocalDate로 변환
    DayOfWeek dayOfWeek = today.getDayOfWeek();

// 주말 체크 (토요일: SATURDAY, 일요일: SUNDAY)
    boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

// 공휴일 체크
    boolean isHoliday = holidays.contains(today);

    if (isWeekend || isHoliday) {
      log.warn("주말 또는 공휴일({})에는 출석 체크를 할 수 없습니다.", today);

      throw new InvalidRequestException(ATTENDANCE_ENTRY_INVALID_DATE);

    }


    // 사용자의 IP 주소 확인 및 예외처리
    String userIp = getClientIp(request); // 클라이언트 IP 가져오기
    log.info("사용자의 IP 주소: {}", userIp);

    if (!isWithinNetwork(userIp)) {
      log.error("User IP 가 허용된 범주 안에 있지 않습니다: {}", userIp);
      throw new InvalidRequestException(ATTENDANCE_NOT_IN_RANGE);
    }



    // 모든 교시 조회
    List<Period> periods = periodRepository.getScheduleByCourseId(now.getDayOfWeek()
        .getDisplayName(TextStyle.FULL, Locale.KOREAN), courseId);
    if (periods.isEmpty()) {
      throw new NoSuchElementException("오늘의 교시 정보가 없습니다.");
    }

    log.debug("조회된 교시 정보: {}", periods);

    switch (attendanceType) {
      case "ENTER" -> {
        handleEnterAttendance(userDetails, periods, now,courseId);
        return AttendanceResponseDTO.builder().enterTime(now).build(); // 입실
      }
      case "EARLY_EXIT" -> {
        handleEarlyLeaveAttendance(userDetails, periods, now, earlyLeavePeriodId,courseId);
        return AttendanceResponseDTO.builder().exitTime(now).build();
      }
      case "EXIT" -> {
        handleExitAttendance(userDetails, periods, now,courseId);
        return AttendanceResponseDTO.builder().exitTime(now).build();
      }
      default -> throw new InvalidRequestException(ATTENDANCE_FAILED);
    }

  }

  /**
   * 조퇴 처리 로직 - 이미 출석 처리된 교시는 상태 유지 - 상태가 없는 교시만 조퇴 처리
   */

  void handleEarlyLeaveAttendance(CustomUserDetails userDetails, List<Period> periods,
      LocalDateTime earlyLeaveTDateTime,
      Long earlyLeavePeriodId, Long courseId) {
    log.info("조퇴 처리 시작");

    // 입실한 기록이 있는지 체크하기
    boolean hasEntryRecord = attendanceRepository.existsByMemberIdAndDateAndCourseIdAndEnterTimeNotNull(
        userDetails.getId(), earlyLeaveTDateTime.toLocalDate(),courseId);
    if (!hasEntryRecord) {
      log.warn("입실한 기록이 없습니다. 조퇴 처리를 할 수 없습니다.");
      throw new InvalidRequestException(ATTENDANCE_ENTRY_NOT_FOUND);
    }

    // 이미 퇴실한 기록이 있으면 중복 방지
    boolean alreadyExited = attendanceRepository.existsByMemberIdAndDateAndCourseIdAndExitTimeNotNull(
        userDetails.getId(), earlyLeaveTDateTime.toLocalDate(),courseId);
    if (alreadyExited) {
      log.warn("조퇴 - 이미 퇴실한 기록이 있습니다. 조퇴를 할 수 없습니다.");
      throw new InvalidRequestException(ATTENDANCE_ALREADY_EXITED);
    }

    // Periods 리스트를 정렬 (시간 순서 보장)
    periods.sort(Comparator.comparing(Period::getStartTime));

    // 조퇴할 교시 찾기
    Period earlyLeavePeriod = periods.stream()
        .filter(p -> p.getId().equals(earlyLeavePeriodId))
        .findFirst()
        .orElseThrow(() -> new InvalidRequestException(ATTENDANCE_EXIT_NOT_FIND_PERIOD));

    log.debug("조퇴 교시 : {}, ID: {}", earlyLeavePeriod.getName(), earlyLeavePeriod.getId());


    /*
    예외처리를 할지 안할지 몰라서 주석처리 해놓음
    // 조퇴 교시의 종료 시간을 기준으로 조퇴 가능 시간 설정
    LocalTime leavePeriodEndTime = exitPeriod.getEndTime();
    LocalTime allowedLeaveStart = leavePeriodEndTime.minusMinutes(10);  // 조퇴 가능 시작 시간 = 조퇴 교시 종료 10분 전
    LocalTime allowedLeaveEnd = leavePeriodEndTime.plusMinutes(60);  // 조퇴 가능 종료 시간 = 조퇴 교시 종료 + 1시간

    if (earlyLeaveTDateTime.toLocalTime().isBefore(allowedLeaveStart) || earlyLeaveTDateTime.toLocalTime().isAfter(allowedLeaveEnd)) {
      log.warn("조퇴 가능 시간이 아닙니다. 조퇴 가능 시간: {} ~ {}", allowedLeaveStart, allowedLeaveEnd);
      throw new InvalidRequestException(ATTENDANCE_EARLY_LEAVE_NOT_ALLOWED);
    }*/

    // ✅ 조퇴 처리 로직
    for (Period period : periods) {
      Attendance attendance = attendanceRepository.findByMemberIdAndPeriodIdAndDate(userDetails.getId(),
              period.getId(), earlyLeaveTDateTime.toLocalDate())
          .orElseGet(() -> new Attendance(null, null, null, null, null, period.getId(),
              period.getCourseId(), userDetails.getId(), null, null, null));

//      // ✅ 이미 출석한 교시에 대해 조퇴할 수 없도록 예외 처리
//      if (period.equals(earlyLeavePeriod) && attendance.getStatus() != null) {
//        throw new InvalidRequestException(ATTENDANCE_EARLY_EXIT_ALREADY_HAS_STATUS);
//      }

      // 모든 교시에 동일한 퇴실(조퇴) 시간 설정
      attendance.updateExitTime(earlyLeaveTDateTime);

      if (attendance.getStatus() == AttendanceStatus.PENDING) {
        if (period.getStartTime().isBefore(earlyLeavePeriod.getStartTime())) {
          // 조퇴 교시 이전의 교시들은 정상 출석처리
          attendance.updateStatus(AttendanceStatus.PRESENT);
        } else {
          // ✅ 조퇴 교시 이후의 교시들은 "조퇴" 처리
          attendance.updateStatus(AttendanceStatus.EARLY_LEAVE);
        }
      }

      // ✅ 조퇴 교시 이전의 교시들은 정상 출석 처리

      // ✅ 조퇴한 시간 업데이트
      /*if (period.equals(earlyLeavePeriod)) {
        attendance.updateExitTime(earlyLeaveTDateTime);
      }*/
      log.info("(조퇴)저장 전 Attendance 객체 확인 - ID: {}, enterTime: {}, status: {}", attendance.getId(),
          attendance.getEnterTime(), attendance.getStatus());
      attendanceRepository.save(attendance);
    }

    log.info("조퇴 처리 완료");

  }


  /**
   * 퇴실 처리 로직
   * - 출석 상태가 null인 교시만 정상 출석으로 처리
   */
  void handleExitAttendance(CustomUserDetails userDetails, List<Period> periods,
      LocalDateTime exitDateTime, Long courseId) {

    log.info("퇴실 처리 시작");

    Optional<Attendance> entryCheck = attendanceRepository.findByMemberIdAndPeriodIdAndDate(
        userDetails.getId(), periods.get(0).getId(), exitDateTime.toLocalDate());

    if (entryCheck.isPresent()) {
      log.debug("🚀 DEBUG: 퇴실 시점에서 첫 번째 교시 Attendance 객체 확인 - ID: {}, enterTime: {}",
          entryCheck.get().getId(), entryCheck.get().getEnterTime());
    } else {
      log.warn("🚨 WARNING: 퇴실 시점에서 첫 번째 교시 Attendance 데이터가 없습니다!");
    }

    // 입실한 기록이 있는지 체크하기
    boolean hasEntryRecord = attendanceRepository.existsByMemberIdAndDateAndCourseIdAndEnterTimeNotNull(userDetails.getId(), exitDateTime.toLocalDate(),courseId);
    log.debug("입실 여부 체크 결과 - hasEntryRecord: {}", hasEntryRecord);
    if (!hasEntryRecord) {
      log.warn("입실한 기록이 없습니다. 퇴실 처리를 할 수 없습니다.");
      throw new InvalidRequestException(ATTENDANCE_ENTRY_NOT_FOUND);
    }


    // 이미 퇴실한 기록이 있으면 중복 방지
    boolean alreadyExited =
        attendanceRepository.existsByMemberIdAndDateAndCourseIdAndExitTimeNotNull(userDetails.getId(), exitDateTime.toLocalDate(),courseId);
    if (alreadyExited) {
      log.warn("이미 퇴실한 기록이 있습니다. 중복 퇴실을 방지합니다.");
      throw new InvalidRequestException(ATTENDANCE_ALREADY_EXITED);
    }

    // Periods 리스트가 시간 순으로 정렬되었는지 확인
    periods.sort(Comparator.comparing(Period::getStartTime));


    // 마지막 교시 찾기
    Period lastPeriod = periods.get(periods.size() - 1);
    log.debug("마지막 교시 : {}, ID: {}", lastPeriod.getName(),lastPeriod.getId());

    // 퇴실 인정 시간 체크
    LocalTime lastPeriodEndTime = lastPeriod.getEndTime();
    LocalTime allowedExitStart = lastPeriodEndTime.minusMinutes(20);  // 퇴실 가능 시작 시간 = 마지막 교시 종료 시간 - 20분
    LocalTime allowedExitEnd = lastPeriodEndTime.plusMinutes(70);  // 퇴실 가능 종료 시간 = 마지막 교시 종료 + 1시간 10분

    if (exitDateTime.toLocalTime().isBefore(allowedExitStart) || exitDateTime.toLocalTime().isAfter(allowedExitEnd)) {
      log.warn("퇴실 가능 시간이 아닙니다. 퇴실 가능 시간: {} ~ {}", allowedExitStart, allowedExitEnd);
      throw new InvalidRequestException(ATTENDANCE_EXIT_NOT_ALLOWED);
    }

    // 기록되지 않은 교시 자동 출석 처리
    for (Period period : periods) {
      Attendance attendance = attendanceRepository.findByMemberIdAndPeriodIdAndDate(userDetails.getId(),
              period.getId(),exitDateTime.toLocalDate())
          .orElseGet(() -> new Attendance(null, null, null, null, null, period.getId(),
              period.getCourseId(), userDetails.getId(), null, null, null));

      if (attendance.getStatus() == AttendanceStatus.PENDING) {
        // 아직 기록되지 않은 교시는 자동 출석 처리
        attendance.updateStatus(AttendanceStatus.PRESENT);
      }

      attendance.updateExitTime(exitDateTime);
      log.info("(퇴실)저장 전 Attendance 객체 확인 - ID: {}, enterTime: {}, status: {}", attendance.getId(),
          attendance.getEnterTime(), attendance.getStatus());
      attendanceRepository.save(attendance);


    }
    log.info("퇴실 처리 완료");
  }

  // private으로 바꾸기
  /**
   * 입실 처리 로직
   * - 현재 교시까지만 출석 상태 기록
   * - 이후 교시는 엔티티만 생성하고 상태는 null로 유지
   */
  void handleEnterAttendance(CustomUserDetails userDetails, List<Period> periods,
      LocalDateTime enterDateTime, Long courseId) {
    log.info("입실 처리 시작");

    log.info("입실하는 날짜시간: {}", enterDateTime);

    // 이미 입실한 기록이 있으면 중복 방지
    boolean alreadyEntered = attendanceRepository.existsByMemberIdAndDateAndCourseId(userDetails.getId(),
        enterDateTime.toLocalDate(),courseId);
    if (alreadyEntered) {
      log.warn("이미 입실한 기록이 있습니다. 중복 입실을 방지합니다.");
      throw new InvalidRequestException(ATTENDANCE_ALREADY_ENTERED);
    }

    // Periods 리스트가 시간 순으로 정렬되었는지 확인
    periods.sort(Comparator.comparing(Period::getStartTime));

    // 1교시 및 마지막 교시 확인
    Period firstPeriod = periods.get(0);
    Period lastPeriod = periods.get(periods.size() - 1);

    // 1교시 시작 40분 전
    LocalDateTime firstAllowedEntryTime = LocalDateTime.of(enterDateTime.toLocalDate(),
        firstPeriod.getStartTime()).minusMinutes(40);
    LocalDateTime lastAllowedEntryTime = LocalDateTime.of(enterDateTime.toLocalDate(),
        lastPeriod.getStartTime()).plusMinutes(20);

    if (enterDateTime.isBefore(firstAllowedEntryTime)) {
      log.warn("1교시 시작 40분 전에는 입실할 수 없습니다. 1교시 시작 시간 - 40분: {}, 현재 시간: {}", firstAllowedEntryTime,
          enterDateTime);
      throw new InvalidRequestException(ATTENDANCE_ENTRY_TOO_EARLY);
    }

    if (enterDateTime.isAfter(lastAllowedEntryTime)) {
      log.warn("마지막 교시 시작 후 20분이 지나 입실이 불가능합니다. 마지막 교시 시작 시간 + 20 분: {}, 현재 시간: {}",
          lastAllowedEntryTime, enterDateTime);
      throw new InvalidRequestException(ATTENDANCE_ENTRY_TOO_LATE);
    }

    // 입실한 교시 찾기
    Period enterPeriod = periods.stream()
        .filter(p -> {
          LocalDateTime periodStart = LocalDateTime.of(enterDateTime.toLocalDate(), p.getStartTime());
          LocalDateTime periodEnd = LocalDateTime.of(enterDateTime.toLocalDate(), p.getEndTime());


          // 1교시 입실 처리 (40분 전부터 허용)
          if(p.equals(firstPeriod)) {
            return(!enterDateTime.isBefore(firstAllowedEntryTime) && enterDateTime.isBefore(periodEnd));
          }

          // 현재 시간이 특정 교시에 속하면 선택
          return (!enterDateTime.isBefore(periodStart) && enterDateTime.isBefore(periodEnd));
        })
        .findFirst()
        .orElseGet(() -> periods.stream()
            .filter(p -> {
              int index = periods.indexOf(p);
              if (index == 0) {
                return false; // 첫 번째, 마지막 교시는 쉬는 시간이 없으므로 제외
              }

              Period previousPeriod = periods.get(index - 1);
              LocalDateTime prevPeriodEnd = LocalDateTime.of(enterDateTime.toLocalDate(), previousPeriod.getEndTime());
              LocalDateTime periodStart = LocalDateTime.of(enterDateTime.toLocalDate(), p.getStartTime());

              return enterDateTime.isAfter(prevPeriodEnd) && enterDateTime.isBefore(periodStart);
            })
            .findFirst()
            .orElseThrow(() -> new InvalidRequestException(ATTENDANCE_ENTRY_NOT_ALLOWED))
        );


    log.info("현재 시간: {}, 입실 교시: {}, 교시 ID: {}", enterDateTime, enterPeriod.getName(),
        enterPeriod.getId());

    for(Period period: periods) {      // 해당학생이 해당 교시에 출석을 한 적이 있는지 검증 없다면 엔티티 생성
      Attendance attendance = attendanceRepository.findByMemberIdAndPeriodIdAndDate(
              userDetails.getId(),
              period.getId(), enterDateTime.toLocalDate())
          .orElseGet(() -> new Attendance(null, null, null, null, null, period.getId(),
              period.getCourseId(), userDetails.getId(), null, null, null));

      attendance.updateEnterTime(enterDateTime);

      LocalDateTime periodStart = LocalDateTime.of(enterDateTime.toLocalDate(),
          period.getStartTime());
      LocalDateTime periodEnd = LocalDateTime.of(enterDateTime.toLocalDate(),
          period.getEndTime());
      LocalDateTime periodStartLate = periodStart.plusMinutes(20); // 교시 시작 후 20분까지 (1교시만)

      if (period.equals(firstPeriod)) { // 1교시 입실 규칙 적용
        if (enterDateTime.isBefore(periodStartLate)) {
          attendance.updateStatus(AttendanceStatus.PRESENT);
        } else if (enterDateTime.isBefore(periodEnd)) {
          attendance.updateStatus(AttendanceStatus.LATE);
        } else {
          attendance.updateStatus(AttendanceStatus.ABSENT);
        }
      } else { // 나머지 교시 입실 규칙 적용
        // 이전 교시 결석 처리
        if (period.getStartTime().isBefore(enterPeriod.getStartTime())) {
          attendance.updateStatus(AttendanceStatus.ABSENT);
        } else if (period.equals(enterPeriod)) {
          // 현재 교시의 상태 판단
          if (enterDateTime.isBefore(periodStartLate)) {
            attendance.updateStatus(AttendanceStatus.PRESENT);
          } else {
            attendance.updateStatus(AttendanceStatus.LATE);
          }
        } else {
          // 이후 교시들은 모두 출석 처리
          attendance.updateStatus(AttendanceStatus.PENDING);
        }

       /* if (enterDateTime.isBefore(periodStart)) {
          attendance.updateStatus(AttendanceStatus.PRESENT);
        } else if (enterDateTime.isBefore(periodStartLate)) {
          attendance.updateStatus(AttendanceStatus.PRESENT);
        } else if (enterDateTime.isBefore(periodEnd)) {
          attendance.updateStatus(AttendanceStatus.LATE);
        } else {
          attendance.updateStatus(AttendanceStatus.ABSENT);
        }*/
      }

      log.info("(입실)저장 전 Attendance 객체 확인 - ID: {}, enterTime: {}, status: {}", attendance.getId(),
          attendance.getEnterTime(), attendance.getStatus());

      attendanceRepository.save(attendance);
    }

/*    // ✅ 저장 후 enterTime이 정상적으로 들어갔는지 확인
    Optional<Attendance> savedAttendance = attendanceRepository.findByMemberIdAndPeriodIdAndDate(
        userDetails.getId(), enterPeriod.getId(), enterDateTime.toLocalDate());

    if (savedAttendance.isPresent()) {
      log.info("🚀 DEBUG: 저장 후 Attendance 객체 확인 - ID: {}, enterTime: {}",
          savedAttendance.get().getId(), savedAttendance.get().getEnterTime());
    } else {
      log.warn("🚨 WARNING: 입실 저장 후 Attendance 데이터가 존재하지 않습니다!");
    }*/

    log.info("입실 처리 완료");

  }

  /**
   * 이전 교시들을 자동으로 결석 처리하는 메서드
   */
  private void updatePreviousPeriodsToAbsent(CustomUserDetails userDetails, Period enterPeriod,
      LocalDateTime enterDateTime, List<Period> allPeriods) {

    // 모든 Period 에서 enterPeriod 의 시작 시간이 이전인 교시들만 필터링
    List<Period> periodsBefore = allPeriods.stream()
        .filter(p -> p.getStartTime().isBefore(enterPeriod.getStartTime()))
        .toList();


    for (Period period : periodsBefore) {
      // 이전 교시의 출석 데이터가 있으면 가져오고, 없으면 새로 생성
      Attendance prevAttendance = attendanceRepository.findByMemberIdAndPeriodIdAndDate(
              userDetails.getId(), period.getId(), enterDateTime.toLocalDate())
          .orElseGet(
              () -> new Attendance(null, null, null, null, null, period.getId(),
                  period.getCourseId(),
                  userDetails.getId(), null, null, null));

      // 이전 교시들은 모두 결석 처리
      prevAttendance.updateStatus(AttendanceStatus.ABSENT);
      attendanceRepository.save(prevAttendance);
    }
  }

  @Value("${attendance.network.allowed-ip-1}")
  private String myIp1;
  @Value("${attendance.network.allowed-ip-1}")
  private String myIp2;
  @Value("${attendance.network.allowed-ip-1}")
  private String myIp3;
  @Value("${attendance.network.allowed-ip-1}")
  private String myIp4;

  private boolean isWithinNetwork(String targetIp) {
    // 학원 와이파이 네트워크 범위를 설정
    // 192.168.1.0/24 네트워크 범위를 사용하는 경우
    String[] allowedNetworks = {
        "127.0.0.1/32", // 로컬, 추가 네트워크 범위가 있을 경우 추가 가능
        "192.168.0.0/24", // 학원 네트워크(로컬네트워크)
        "115.93.9.236/30",  // 학원 와이파이 공인 ip
        myIp1,
        myIp2,
        myIp3,
        myIp4,
    };
    log.info("myIp1: {}, myIp2: {}, myIp3: {}, myIp4: {}",myIp1,myIp2,myIp3,myIp4);

    try {
      InetAddress targetAddress = InetAddress.getByName(targetIp);

      for (String network : allowedNetworks) {
        String[] parts = network.split("/");
        InetAddress networkAddress = InetAddress.getByName(parts[0]);
        int prefixLength = Integer.parseInt(parts[1]);

        if (isInRange(targetAddress, networkAddress, prefixLength)) {
          return true; // IP가 지정된 네트워크 범위 내에 있으면 true 반환
        }
      }
    } catch (UnknownHostException e) {
      log.error("허용 네트워크 감지 중 에러 발생: {}", e.getMessage());
    }

    // 만약 클라이언트 IP가 어느 네트워크 범위에도 포함되지 않으면 false 반환
    return false;
  }

  private boolean isInRange(InetAddress target, InetAddress network, int prefixLength) {
    byte[] targetBytes = target.getAddress();
    byte[] networkBytes = network.getAddress();

    int byteCount = prefixLength / 8;
    int bitRemainder = prefixLength % 8;

    for (int i = 0; i < byteCount; i++) {
      if (targetBytes[i] != networkBytes[i]) return false;
    }

    if (bitRemainder > 0) {
      int mask = (1 << (8 - bitRemainder)) - 1;
      if ((targetBytes[byteCount] & ~mask) != (networkBytes[byteCount] & ~mask)) return false;
    }

    return true;
  }

  private String getClientIp(HttpServletRequest request) {
    // X-Forwarded-For 헤더에서 클라이언트의 실제 IP를 추출합니다.
    String ip = getHeaderValue(request, "X-Forwarded-For");
    if (ip != null) {
      return ip.split(",")[0].trim(); // 첫 번째 IP 주소를 사용
    }

    // 여러 프록시 서버를 거쳤을 경우를 고려한 다른 헤더 체크
    ip = getHeaderValue(request, "Proxy-Client-IP");
    if (ip != null) {
      return ip;
    }

    ip = getHeaderValue(request, "WL-Proxy-Client-IP");
    if (ip != null) {
      return ip;
    }

    ip = getHeaderValue(request, "HTTP_CLIENT_IP");
    if (ip != null) {
      return ip;
    }

    ip = getHeaderValue(request, "HTTP_X_FORWARDED_FOR");
    if (ip != null) {
      return ip;
    }

    // 최종적으로 RemoteAddr에서 IP를 추출
    ip = request.getRemoteAddr();

    // 로컬 IPv6 주소 (::1)을 IPv4 주소(127.0.0.1)로 변환
    if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
      ip = "127.0.0.1";
    }

    log.info("감지된 IP 주소: {}",ip);
    return ip;
  }

  private String getHeaderValue(HttpServletRequest request, String header) {
    String value = request.getHeader(header);
    if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
      return value;
    }
    return null;
  }

  //  관리자, 수강생
  /* 출석 상세 조회 */
  public AttendDetailDTO findAttendanceByIds(CustomUserDetails userDetails, Long courseId,
      Long studentId, LocalDate date, Pageable pageable) {
    /* 교육과정Id, 날짜, 수강생번호 */
    /* 수강생 정보, 시간표 정보, 출석 목록 정보 */

    log.info("출석 상세 조회 요청");
    String role = extractRole(userDetails);
    if (role.equals("ADMIN")) {
      log.info("요청한 사람의 Role(ADMIN 인지 확인) :{}", role);
    } else if (role.equals("STUDENT")) {
      log.info("요청한 사람의 Role(STUDENT 인지 확인) :{}", role);
    }

    log.info("출석 상세 조회를 시도합니다.");
    log.debug("출석 상세 조회 요청된 정보: courseId={}, studentId={}, date={}", courseId, studentId, date);

    // 출석 상태 목록
    Page<AttendanceStatusListDTO> results = attendanceRepository.findAttendanceDetailByIds(courseId,
        studentId, date, pageable);

    /*if (results.isEmpty()) {
      log.warn("경고 : 교시ID: {}, 수강생ID: {}, 출석날짜(date): {}에 출석이 비어있습니다.", courseId,studentId,date);
      throw new NoSuchElementException("출석이 비어있습니다.");
    }*/

    log.info("조회된 출석 데이터 {}", results.getContent());

    // 수강생 정보에 출력될 데이터 (수강생명, 과정명, 날짜, 담당자명)
    MemberInfoInDetail tuple = attendanceRepository.findMemberInfoByAttendance(courseId, studentId,
        date);

    // 교시 리스트 조회
    ScheduleResponseDTO periodsByCourseId = scheduleRepository.findScheduleWithPeriodsByCourseId(
        courseId);
    List<PeriodResponseDTO> periodList = periodsByCourseId.getPeriods();

    return AttendDetailDTO.builder()
        .memberInfo(tuple)
        .periodList(periodList)
        .attendances(results)
        .build();
  }

  /*public List<AttendanceResponseDTO> findAttendanceById(AttendanceRequestDTO dto) {
   *//* 교시번호와 수강생번호 *//*
   *//* 수강생 정보, 시간표 정보, 출석 목록 정보 *//*

    log.info("출석 상세 조회를 시도합니다.");
    log.debug("출석 상세 조회 요청된 정보: {}", dto);

    log.info("교시ID: {}, 수강생ID: {}, 출석날짜(date): {} 로 출석 상세 조회하기",
        dto.getCourseId(), dto.getMemberId(), dto.getDate());
    log.debug("courseID: {} memberId: {} date: {}", dto.getCourseId(), dto.getMemberId(),
        dto.getDate());
    List<Attendance> results = attendanceRepository.findAllAttendance(dto);

    if (results.isEmpty()) {
      log.warn("경고 : 교시ID: {}, 수강생ID: {}, 출석날짜(date): {}에 출석이 비어있습니다.", dto.getCourseId(),
          dto.getMemberId(), dto.getDate());
      throw new NoSuchElementException("출석이 비어있습니다.");
    }

    log.info("{} 개의 출석 조회 중", results.size());

    List<AttendanceResponseDTO> responseDTOList = results.stream()
        .map(AttendanceResponseDTO::fromEntity)
        .toList();
    log.info("{} 개의 출석 조회 완료", responseDTOList.size());

    return responseDTOList;
  }*/

  public String extractRole(@AuthenticationPrincipal CustomUserDetails userDetails) {
    return userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority) // Role 이름 추출
        .findFirst() // Role이 여러 개인 경우 첫 번째만 가져옴
        .orElseThrow(() -> new IllegalStateException("사용자 권한이 없습니다."));
  }

  public List<CourseListDTO> getAllCoursesByAdminId(CustomUserDetails userDetails) {
    return courseRepository.findCoursesByAdminId(userDetails.getId());

  }

  public List<CourseListDTO> getAllCoursesByStudentId(CustomUserDetails userDetails) {
    return courseRepository.findCoursesByStudentId(userDetails.getId());
  }

  public List<AttendanceTableDTO> getAttendanceByCourseAndDate(CustomUserDetails userDetails,
      Long courseId, LocalDate date) {
    log.info("main에 출석상태와 교시를 가져가기");
    log.debug("학생 ID: {}, 교육과정 ID: {}, 날짜: {}", userDetails.getId(), courseId, date);

    // 해당 날짜의 요일 (한국어로)
    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + "요일";
    log.debug("달력에서 선택한 요일: {}", dayOfWeek);

    List<AttendanceTableDTO> attendanceMainDTOS = attendanceRepository.findAttendanceStatusByPeriods(
        userDetails.getId(), courseId, date, dayOfWeek);

    log.debug(attendanceMainDTOS.toString());

    return attendanceMainDTOS;
  }

  @Transactional
  public List<PeriodResponseDTO> getPeriodsByDateAndDayOfWeek(CustomUserDetails userDetails,
      Long courseId) {
    log.info("main 에서 사용하기 위해 시간표 조회하기");
    log.debug("학생 ID: {}, 교육과정 ID: {}", userDetails.getId(), courseId);

    List<Period> periods = periodRepository.getScheduleByCourseId(null, courseId);

    log.debug(periods.toString());

    return periods.stream().map(PeriodResponseDTO::fromEntity).toList();
  }

  @Transactional
  public Map<String, Object> getStudentAttendanceRates(Long memberId, Long courseId) {

    log.info("출석률 조회 요청");
    log.debug("memberId: {}, courseId: {}",memberId,courseId);

    Course course = courseRepository.findCourseById(courseId);
    LocalDate startDate = course.getStartDate();
    LocalDate endDate = course.getEndDate();

    List<AttendanceDailyStats> dailyStats = attendanceRepository.findAttendanceStatsByMemberAndCourse(memberId, courseId);
    dailyStats.forEach(stat -> log.debug("학생-AttendanceDailyStats: {}", stat));

    int year = startDate.getYear(); // 교육과정의 연도를 기준으로 공휴일 가져오기

    log.debug("교육과정 기간 기준 연도: {}", year);

    // ✅ 해당 연도의 공휴일 정보를 DB에서 가져오기
    Set<LocalDate> holidays = holidayService.getHolidaysForYear(year);

    return AttendanceCalculator.calculateAttendanceRates(dailyStats, startDate, endDate,holidays);
  }


  public Map<Long, Map<String, Object>> getAllStudentsAttendanceRates(Long courseId) {

    log.info("관리자용 출석률 조회 요청 - Course ID: {}", courseId);

    Course course = courseRepository.findCourseById(courseId);
    if (course == null) {
      log.warn("코스 ID {}에 해당하는 과정을 찾을 수 없습니다.", courseId);
      return new HashMap<>();
    }
    LocalDate startDate = course.getStartDate();
    LocalDate endDate = course.getEndDate();

    // 해당 강좌의 모든 학생별 출석 데이터 조회
    List<AttendanceDailyStats> dailyStatsList = attendanceRepository.findAttendanceStatsByCourse(courseId);
    //dailyStatsList.forEach(stat -> log.debug("관리자-AttendanceDailyStats: {}", stat));
    if (dailyStatsList.isEmpty()) {
      log.warn("과정 ID {}에 대한 출석 데이터가 없습니다.", courseId);
      return new HashMap<>();
    }

    // 학생별 출석률 계산 결과를 저장할 Map
    Map<Long, Map<String, Object>> studentAttendanceRates = new HashMap<>();

    // 학생별로 데이터를 그룹화
    Map<Long, List<AttendanceDailyStats>> groupedStats = dailyStatsList.stream()
        .collect(Collectors.groupingBy(AttendanceDailyStats::getStudentId));

    // 학생별 출석률 계산
    for (Map.Entry<Long, List<AttendanceDailyStats>> entry : groupedStats.entrySet()) {
      Long studentId = entry.getKey();
      List<AttendanceDailyStats> studentStats = entry.getValue();

      log.debug("학생 {} 출석률 계산 시작", studentId);

      try {
        int year = startDate.getYear(); // 교육과정의 연도를 기준으로 공휴일 가져오기
        log.debug("(관리자) - 교육과정 기간 기준 연도: {}", year);

        // 해당 연도의 공휴일 정보를 DB에서 가져오기
        Set<LocalDate> holidays = holidayService.getHolidaysForYear(year);

        Map<String, Object> attendanceRate = AttendanceCalculator.calculateAttendanceRates(
            studentStats, startDate, endDate, holidays);
        studentAttendanceRates.put(studentId, attendanceRate);

        log.debug("학생 {} 출석률 계산 완료: {}", studentId, attendanceRate);
      } catch (Exception e) {
        // 예외 발생 시 해당 학생은 기본값으로 처리하고 다음 학생으로 넘어감
        log.error("학생 {} 출석률 계산 중 오류 발생: {}", studentId, e.getMessage(), e);

        Map<String, Object> defaultRate = Map.of(
            "validAttendanceDays", 0,
            "overallAttendanceRate", 0.0,
            "twentyDayRate", 0.0,
            "twentyDayRates", new ArrayList<>()
        );
        studentAttendanceRates.put(studentId, defaultRate);
      }
    }

    return studentAttendanceRates;

  }

  public List<Map<String, Object>> getTwentyTermsByCourseId(Long courseId) {
    log.info("관리자용 20일 단위 기간 조회 요청 - Course ID: {}", courseId);

    // 1. 과정 조회해서  startDate 와 endDate 가지고 오기
    Course course = courseRepository.findCourseById(courseId);
    LocalDate startDate = course.getStartDate();
    LocalDate endDate = course.getEndDate();

    int year = startDate.getYear(); // 교육과정의 연도를 기준으로 공휴일 가져오기
    log.debug("(관리자 20일 단위 차수 계산) - 교육과정 기간 기준 연도: {}", year);

    Set<LocalDate> holidays = holidayService.getHolidaysForYear(year);

    // 2. getValidDays 사용하기
    List<LocalDate> validDays = getValidDays(startDate, endDate, holidays);

    // 3. calculateTwentyDaySegments 가져오기
    return calculateTwentyDaySegments(validDays);

  }

  public AttendancePrintResponseDto getAttendancePrintDataByCourseId(Long courseId,
      String termLabel) {

    log.info("courseId: {}, term:{} 에 대한 프린트 데이터 요청", courseId, termLabel);

    // 과정 정보 조회하기
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> {
          log.error("에러: 교육과정 ID {} 에 대한 과정을 찾을 수 없습니다.", courseId);
          return new InvalidRequestException(COURSE_NOT_FOUND);
        });

    // 1. 20일 단위 조각 가지고 오기
    List<Map<String, Object>> termsByCourseId = getTwentyTermsByCourseId(courseId);

    // 2. term 에 해당하는 기간 가지고 오기
    Map<String, Object> result = null;
    for (Map<String, Object> map : termsByCourseId) {
      if (termLabel.equals(map.get("차수"))) {
        result = map;
        break; // 첫 번째 값만 찾으면 반복 종료
      }
    }

    // 해당하는 기간이 없을 때
    if (result == null) {
      throw new InvalidRequestException(ATTENDANCE_SEGMENT_NOT_FOUND);
    }

    log.info("term에 해당하는 기간  정보: {}", result);

    // 해당 반을 수강하는 학생 List 조회하기
    List<Member> students = enrollRepository.findStudentByCourseId(courseId);

    log.debug("해당 반을 수강하는 학생 List(students) : {}", students.toString());

    Object periodDays = result.get("날짜들");

    List<LocalDate> courseDates;
    if (periodDays instanceof List<?>) {
      courseDates = ((List<?>) periodDays)
          .stream()
          .filter(LocalDate.class::isInstance) // LocalDate 타입인지 확인
          .map(LocalDate.class::cast) // 안전한 형변환
          .toList();
    } else {
      courseDates = new ArrayList<>(); // 기본값 설정
    }
    log.debug("해당 기간에 속하는 날짜들: {}", courseDates);
    log.info("해당 기간에 속하는 날짜들 사이즈: {}", courseDates.size());

    LocalDate startDate;
    // 해당 차수의 시작일과 종료일
    /*if (termLabel.equals("1차")) {
      startDate = ((LocalDate) result.get("시작일")).minusDays(1);
      List<LocalDate> newCourseDates = new ArrayList<>(courseDates);
      newCourseDates.add(0, startDate);
      courseDates = newCourseDates;
      log.info("1차라서 교육과정 시작일 {} 을 courseDates에 포함시켰음 : {}", startDate, courseDates.size());
      //courseDates = courseDates.subList(1, courseDates.size()); // 시작날짜 포함
    } else {
      startDate = (LocalDate) result.get("시작일");
    }*/
    startDate = (LocalDate) result.get("시작일");
    LocalDate endDate = (LocalDate) result.get("종료일");

    log.debug("시작일: {}", startDate);
    log.debug("종료일: {}", endDate);

    // 페이지별 데이터 생성 (5일씩 분할)
    List<AttendancePrintResponseDto.PrintAttendancePageDto> pages = new ArrayList<>();

    // 5일에 한 장씩 출력이니까
    for (int pageIndex = 0; pageIndex < courseDates.size(); pageIndex += 5) {
      int endIdx = Math.min(pageIndex + 5, courseDates.size());
      List<LocalDate> pageDates = courseDates.subList(pageIndex, endIdx);
      /* pageDates 변화
      1회전: 0,4
      2회전: 5,9
      3회전: 10, 14
      4회전: 15, 19
      * */

      AttendancePrintResponseDto.PrintAttendancePageDto pageDto =
          AttendancePrintResponseDto.PrintAttendancePageDto.builder()
              .pageNumber(pageIndex / 5 + 1)
              .pageStartDate(pageDates.get(0))
              .pageEndDate(pageDates.get(pageDates.size() - 1))
              .build(); // 나중에 students 업데이트하기

      List<AttendancePrintResponseDto.PrintStudentAttendanceDto> studentStats = new ArrayList<>();

      for (Member student : students) {
        AttendancePrintResponseDto.PrintStudentAttendanceDto studentDto =
            AttendancePrintResponseDto.PrintStudentAttendanceDto.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .build();   // 나머지 항목 업데이트하기

        List<AttendancePrintResponseDto.PrintDailyAttendanceDto> dailyAttendance = new ArrayList<>();
        int lateCount = 0;
        int earlyLeaveCount = 0;
        int absentCount = 0;

        // 각 날짜별 출석 데이터 조회
        for (LocalDate date : pageDates) {
          log.info("해당 날짜의 출석 데이터 조회: {}", date);
          AttendancePrintResponseDto.PrintDailyAttendanceDto dailyDto =
              AttendancePrintResponseDto.PrintDailyAttendanceDto.builder()
                  .date(date)
                  .build(); // 나머지 항목 업데이트하기

          List<Tuple> attendances = attendanceRepository.findAttendanceStatusByStudentIdAndCourseIdAndDate(
              student.getId(), courseId, date);

          // 하루동안의 8교시 출석상태
          List<AttendancePrintResponseDto.PeriodAttendanceDto> periods = new ArrayList<>();
          String dailySummary = "출석"; // 기본값
          int late = 0;
          int earlyLeave = 0;
          int absent = 0;

          // 하루동안의 8교시 출석상태
          for (Tuple tuple : attendances) {

            String status = Objects.requireNonNull(tuple.get(attendance.status)).toKorean();
            String periodName = tuple.get(period.name);
            Long periodId = tuple.get(period.id);

            AttendancePrintResponseDto.PeriodAttendanceDto periodDto =
                AttendancePrintResponseDto.PeriodAttendanceDto.builder()
                    .period(extractPeriodNumber(periodName))
                    .status(status)
                    .build();

            periods.add(periodDto);

            // 지각/조퇴 상태 업데이트
            if ("지각".equals(status)) {
              late++;
            } else if ("조퇴".equals(status)) {
              earlyLeave++;
            } else if ("결석".equals(status)) {
              absent++;
            }
          } // 교시 반복문 끝
          // late, earlyLeave, absent 카운트 비교로 dailySummary 설정 및 count 설정
          boolean isAbsent = absent == TOTAL_SESSIONS_PER_DAY;
          boolean isLate = !isAbsent && (absent > 0 || late > 0);
          boolean isEarlyLeave = !isAbsent && earlyLeave > 0;

          if (isAbsent) {
            dailySummary = "결석";
            absentCount++;
          } else if (isLate) {
            dailySummary = "지각";
            lateCount++;
          } else if (isEarlyLeave) {
            dailySummary = "조퇴";
            earlyLeaveCount++;
          }
          dailyDto = dailyDto.toBuilder()
              .periods(periods)
              .dailySummary(dailySummary)
              .build();

          dailyAttendance.add(dailyDto);

        } // 하루씩 5일

        int processedDays = pageDates.size(); // 해당 페이지의 소정 출석일 (날짜수)

        // 지각과 조퇴를 합산하여 3회당 결석 1회로 계산
        int lateAndEarlyLeaveTotal = lateCount+earlyLeaveCount;
        int additionalAbsentDays = lateAndEarlyLeaveTotal / 3;  // 3회당 결석 1회

        // 실제 결석일 = 직접 결석한 일수 + 지각/조퇴로 인한 추가 결석일
        int totalAbsentDays = absentCount + additionalAbsentDays;

        // 실제출석일 = 소정출석일 - 총 결석일
        int realAttendDays = processedDays - totalAbsentDays;

        studentDto = studentDto.toBuilder()
            .processedDays(processedDays)
            .realAttendDays(realAttendDays)
            .dailyAttendance(dailyAttendance)
            .lateCount(lateCount)
            .earlyLeaveCount(earlyLeaveCount)
            .absentCount(absentCount)
            .build();

        studentStats.add(studentDto);

      }
      pageDto = pageDto.toBuilder()
          .students(studentStats)
          .build();

      pages.add(pageDto);

    }

    // 요약 페이지 생성
    AttendancePrintResponseDto.SummaryPageDto summaryPage = createSummaryPage(students, courseId,
        startDate, endDate, courseDates, termLabel);

    // 최종 응답 DTO 생성
    AttendancePrintResponseDto responseDto =
        AttendancePrintResponseDto.builder()
            .courseName(course.getName())
            .termLabel(termLabel)
            .startDate(course.getStartDate())
            .endDate(course.getEndDate())
            .termStartDate(startDate)
            .termEndDate(endDate)
            .pages(pages)
            .summaryPage(summaryPage)
            .build();

    return responseDto;

  }

  private SummaryPageDto createSummaryPage(List<Member> students, Long courseId,
      LocalDate startDate, LocalDate endDate,
      List<LocalDate> courseDates, String termLabel) {
    log.info("프린트 마지막 장 요약본 메소드 진입");

    AttendancePrintResponseDto.SummaryPageDto summaryPage = new AttendancePrintResponseDto.SummaryPageDto();
    List<AttendancePrintResponseDto.StudentSummaryDto> summaries = new ArrayList<>();

/*    if (termLabel.equals("1차")) {
      courseDates.subList(1, courseDates.size());
      startDate = startDate.plusDays(1);
      log.info("출석률 계산을 위해 1차라서 포함시켰던 교육과정 시작일 {} 을 배제시킴 : {}", startDate, courseDates.size());
      //courseDates = courseDates.subList(1, courseDates.size());
    }*/

    int totalWorkingDays = courseDates.size();
    log.debug("소정 출석일: {}", totalWorkingDays);

    for (Member student : students) {
      AttendancePrintResponseDto.StudentSummaryDto summary =
          AttendancePrintResponseDto.StudentSummaryDto.builder()
              .studentId(student.getId())
              .studentName(student.getName())
              .totalWorkingDays(totalWorkingDays)
              .build();

      // 한 사람당 기간 동안의 출석 정보 총 20일치
      List<AttendanceDailyStats> dailyStats = attendanceRepository.findAttendanceStatsByStudentIdAndCourseIdAndDates(
          student.getId(), courseId, startDate, endDate);
      log.info("dailyStats 사이즈: {}", dailyStats.size());

      Map<String, Object> twentyDayMap = calculateTwentyDayAttendanceRatesForPrint(dailyStats,
          courseDates);

      int attendanceDays = (int) twentyDayMap.get("attendanceDays");
      int lateDays = (int) twentyDayMap.get("lateDays");
      int earlyLeaveDays = (int) twentyDayMap.get("earlyLeaveDays");
      int absentDays = (int) twentyDayMap.get("absentDays");
      double attendanceRate = (double) twentyDayMap.get("attendanceRate");

      summary = summary.toBuilder()
          .attendanceDays(attendanceDays)
          .lateDays(lateDays)
          .earlyLeaveDays(earlyLeaveDays)
          .absentDays(absentDays)
          .attendanceRate(attendanceRate)
          .build();

      summaries.add(summary);

    }
    summaryPage = summaryPage.toBuilder().studentSummaries(summaries).build();
    log.info("요약본 만들기 로직 종료");
    return summaryPage;

  }

  // 교시명에서 숫자 추출 (예: "1교시" -> 1)
  private int extractPeriodNumber(String periodName) {
    log.info("교시명에서 숫자 추출하는 로직 시작");
    // 정규식으로 숫자만 추출
    Pattern pattern = Pattern.compile("\\d+");  // \d 숫자 의미, + 1개 이상의 숫자가 연속될 경우 => 문자열에서 숫자 찾아냄
    Matcher matcher = pattern.matcher(periodName);  // periodName 에서 숫자를 찾기

    if (matcher.find()) {
      log.info("교시명에서 숫자 추출 성공: 교시명: {}, 숫자: {}", periodName, Integer.parseInt(matcher.group()));
      return Integer.parseInt(matcher.group()); // matcher.group() 찾은 숫자 부분을 문자열로 반환 후 정수로 변환
    }

    return 0; // 기본값
  }

  /**
   * 매일 밤 11시에 실행되어 미기록 출석을 자동으로 처리합니다.
   * cron 표현식: 초 분 시 일 월 요일
   */
  @Scheduled(cron = "0 0 23 * * MON-FRI")  // 월-금 11시에 실행
  @Transactional
  public void processUnmarkedAttendance() {
    log.info("미기록 출석 자동 처리 시작");
    LocalDate today = LocalDate.now();
    log.info("현재 날짜: {}",today);

    int year = today.getYear(); // 교육과정의 연도를 기준으로 공휴일 가져오기

    log.debug("오늘의 연도: {}", year);

    // ✅ 해당 연도의 공휴일 정보를 DB에서 가져오기
    Set<LocalDate> holidays = holidayService.getHolidaysForYear(year);


    /*
     * holidays나 주말일 경우 결석처리 생략
     * */
    DayOfWeek dayOfWeek = today.getDayOfWeek();

// 주말 체크 (토요일: SATURDAY, 일요일: SUNDAY)
    boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

// 공휴일 체크
    boolean isHoliday = holidays.contains(today);

    if (isWeekend || isHoliday) {
      if (isWeekend) {
        log.warn("주말({})이므로 결석 처리를 생략합니다.", today);
      } else {
        log.warn("공휴일({})이므로 결석 처리를 생략합니다.", today);
      }
      return; // 여기서 메소드 종료 - 이후 로직 실행하지 않음
    }

    try {
      // 오늘 날짜의 모든 활성 수업 조회
      List<Course> todayCourses = courseRepository.findAllActiveCourses();
      log.info("오늘의 활성 수업 수: {}", todayCourses.size());

      for (Course course : todayCourses) {
        // 해당 수업의 모든 학생 ID와 교시 조회
        List<Long> studentIds = enrollRepository.findStudentIdByCourseId(course.getId());
        List<Period> periods = periodRepository.findByCourseId(course.getId());

        log.info("수업 ID: {}, 학생 수: {}, 교시 수: {}",
            course.getId(), studentIds.size(), periods.size());

        for (Long studentId : studentIds) {
          for (Period period : periods) {
            // 해당 학생/교시에 대한 출석 기록이 있는지 확인
            Optional<Attendance> attendance = attendanceRepository
                .findByMemberIdAndDateAndPeriodId(studentId, today, period.getId());

            // 출석 기록이 없으면 자동으로 "결석" 처리
            if (attendance.isEmpty()) {
              AttendanceDTO dto = AttendanceDTO.builder().
                  memberId(studentId).
                  date(today).
                  periodId(period.getId()).
                  courseId(course.getId()).
                  status("ABSENT").build();

              Attendance newAttendance = toEntityForAbsent(dto);

              attendanceRepository.save(newAttendance);
              log.info("자동 결석 처리: 학생 ID={}, 날짜={}, 교시={}",
                  studentId, today, period.getName());
            }
          }
        }
      }
      log.info("미기록 출석 자동 처리 완료");
    } catch (Exception e) {
      log.error("미기록 출석 자동 처리 중 오류 발생", e);
    }
  }

  private Attendance toEntityForAbsent(AttendanceDTO attendanceDTO){
    log.info("attendanceDTO: {}",attendanceDTO);
    return  new Attendance(
        null,
        attendanceDTO.getStatus() == null ? null : AttendanceStatus.fromENStatus(attendanceDTO.getStatus()),
        attendanceDTO.getDate() == null ? null : attendanceDTO.getDate(),
        null,
        null,
        attendanceDTO.getPeriodId(),
        attendanceDTO.getCourseId(),
        attendanceDTO.getMemberId(),
        null,
        null,
        null
    );
  }

  /**
   * 수동으로 특정 날짜의 미기록 출석을 처리하는 메소드
   * 관리자 API에서 호출할 수 있습니다.
   */
  @Transactional
  public void processUnmarkedAttendanceForDate(LocalDate date) {
    log.info("{}일 미기록 출석 수동 처리 시작", date);

    // 위의 자동 처리와 동일한 로직을 수행하지만, 오늘 날짜 대신 지정된 날짜 사용
    try {
      List<Course> courses = courseRepository.findAllActiveCourses();

      for (Course course : courses) {
        List<Long> studentIds = enrollRepository.findStudentIdByCourseId(course.getId());
        List<Period> periods = periodRepository.findByCourseId(course.getId());

        // 나머지 로직은 위와 동일
        // ...
      }

      log.info("{}일 미기록 출석 수동 처리 완료", date);
    } catch (Exception e) {
      log.error("{}일 미기록 출석 수동 처리 중 오류 발생", date, e);
    }
  }
}



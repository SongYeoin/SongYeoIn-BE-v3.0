package com.syi.project.attendance.service;

import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_NOT_IN_RANGE;
import static com.syi.project.common.exception.ErrorCode.ATTENDANCE_NOT_YET;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.StudentAllAttendRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
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
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  // 담당자
  /* 출석 전체 조회 */
  public Page<AttendListResponseDTO> getAllAttendancesForAdmin(CustomUserDetails userDetails,
      Long courseId, AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable) {
    /* 담당자는 courseId, studentId, date, member_name, attendance_status */

    log.info("관리자 전체 출석 조회");
    Long adminId = userDetails.getId();
    log.debug("관리자 Id: {}", adminId);
    log.debug("courseId: {}, date: {}", courseId, dto.getDate());
    log.debug("필터링 조건 : studentName={}, status ={}", dto.getStudentName(), dto.getStatus());

    // 1교시, 2교시... 교시명 모음
    // 해당 날짜의 요일 (한국어로)
    String dayOfWeek = dto.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN)+"요일";
    log.debug("해당하는 요일: {}",dayOfWeek);

    List<String> periods = periodRepository.findPeriodsByDayOfWeek(courseId, dayOfWeek);
    log.debug("{} 의"
        + " 교시명 모음: {}",dayOfWeek, periods);

    return attendanceRepository.findPagedAdminAttendListByCourseId(courseId, dto, periods,
        pageable);

  }


  // 수강생
  // 출석 전체 조회
  public Page<AttendListResponseDTO> getAllAttendancesForStudent(CustomUserDetails userDetails,
      Long courseId, StudentAllAttendRequestDTO dto, Pageable pageable) {

    log.info("수강생 전체 출석 조회");
    log.debug("courseId: {}, startDate: {}, endDate: {}", courseId, dto.getStartDate(),
        dto.getEndDate());
    log.debug("필터링 조건 : status ={}", dto.getStatus());

    Long studentId = userDetails.getId();
    log.debug("수강생 Id: {}", studentId);

    // 1교시, 2교시... 교시명 모음
    List<String> periods = periodRepository.findPeriodsInRange(courseId,dto.getStartDate(),
        dto.getEndDate());
    log.debug("조회된 교시 모음: {}", periods);

    log.info("dto 변환");
    AllAttendancesRequestDTO requestDTO = AllAttendancesRequestDTO.builder()
        .date(null)
        .studentId(studentId)
        .startDate(dto.getStartDate())
        .endDate(dto.getEndDate())
        .status(dto.getStatus())
        .build();

    return attendanceRepository.findPagedAdminAttendListByCourseId(courseId, requestDTO, periods,
        pageable);

  }

  //  관리자
  /* 학생별 출석 조회 => 출석 전체 조회랑 같이 만들어질 확률 많음 */
  /*public AttendanceResponseDTO getAttendanceByCourseIdAndMemberId(AttendanceRequestDTO dto) {
    List<Attendance> results = attendanceRepository.findAllAttendance()
    return null;
  }*/

  /* 교시번호와 수강생 번호로 단일 출석 조회하기 */
  public AttendanceResponseDTO getAttendanceByPeriodAndMember(AttendanceRequestDTO dto) {
    log.info("PeriodID와 MemberID 로 단일 출석 조회를 시도합니다.");
    log.debug("단일 출석 조회 요청된 정보: {}", dto);

    log.info("요청된 정보로 출석 조회");
    /*List<Attendance> results = attendanceRepository.findAllAttendance(dto);

    if (results.isEmpty()) {
      log.warn("경고 : PeriodID {} 와 MemberID {} 로 조회한 결과가 없습니다.", dto.getPeriodId(),
          dto.getMemberId());
      throw new NoSuchElementException("조회된 출석이 없습니다.");
    }

    log.info("{} 개의 시간표 조회 중", results.size());

    // 조회한 결과 dto로 변환
    return fromEntity(results.get(0));*/
    return null;

  }

  //  관리자
  /* 출석 수정 */
  @Transactional
  public AttendanceResponseDTO updateAttendance(Long attendanceId, String status) {
    /* attendanceId status */
    log.info("{}에 대한 출석 상태를 수정합니다.", attendanceId);
    log.debug("출석 상태 수정 요청된 정보: {}", status);

    Attendance attendance = attendanceRepository.findById(attendanceId)
        .orElseThrow(() -> {
          log.error("에러: 출석 ID {} 에 대한 출석을 찾을 수 없습니다.", attendanceId);
          return new NoSuchElementException("Attendance not found with id " + attendanceId);
        });
    log.info("출석 ID {}을 찾았습니다.", attendanceId);
    AttendanceStatus newStatus = AttendanceStatus.fromENStatus(status); // 한글을 Enum으로 변환
    attendance.updateStatus(newStatus);
    attendance.updateModifiedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
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
  @Transactional
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
  }

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
    log.debug("yesterday={}, courseId={}, studentId={}, periodId={}",yesterday,courseId,student.getId(),periodId);
    Attendance attendance = new Attendance(null, AttendanceStatus.ABSENT, yesterday, null, null,
        periodId, courseId,
        student.getId(), null);
    log.debug("결석상태로 저장하려는 출석의 정보: attendance={}",attendance);
    Attendance savedAttendance = attendanceRepository.save(attendance);
    log.info("성공적으로 결석 상태를 처리했습니다.");
    log.debug("결석 처리한 출석의 정보: savedAttendance={}",savedAttendance);
  }

  private String convertDayOfWeekToString(DayOfWeek dayOfWeek) {
    log.info("convertDayOfWeekToString");
    log.debug("dayOfWeek: {} ", dayOfWeek);
    return switch (dayOfWeek) {
      case MONDAY -> "월";
      case TUESDAY -> "화";
      case WEDNESDAY -> "수";
      case THURSDAY -> "목";
      case FRIDAY -> "금";
      case SATURDAY -> "토";
      case SUNDAY -> "일";
    };
  }

  //  수강생
  /* 출석 등록 */
  @Transactional
  public void createAttendance(CustomUserDetails userDetails, Long periodId,
      HttpServletRequest request) {
    log.info("출석 등록을 시도합니다.");
    log.debug("출석 등록 요청된 periodId: {}", periodId);

    // 해당 교시가 현재 출석 가능 시간인지 확인
    log.info("교시 ID {} 를 조회합니다.", periodId);
    Optional<Period> periodOptional = periodRepository.findById(periodId);
    if (periodOptional.isEmpty()) {
      throw new NoSuchElementException("해당 교시를 찾을 수 없습니다.");
    }

    AttendanceStatus status;
    log.debug("조회된 교시 정보: {}", periodOptional.get());
    Period period = periodOptional.get();

    // period에서 courseId, userDetails에서 수강생 id
    log.debug("출석에 저장될 courseId: {}", period.getCourseId());
    log.debug("출석에 저장될 memberId: {}", userDetails.getId());

    // 사용자의 IP 주소 확인
    String userIp = getClientIp(request); // 클라이언트 IP 가져오기
    log.info("사용자의 IP 주소: {}", userIp);

    if (!isWithinNetwork(userIp)) {
      log.error("User IP 가 허용된 범주 안에 있지 않습니다: {}", userIp);
      throw new InvalidRequestException(ATTENDANCE_NOT_IN_RANGE);
    }

    status = checkIfWithinTimeWindow(period);
    log.debug("출석에 저장될 status: {}", status);

    Attendance attendance = AttendanceRequestDTO.builder()
        .periodId(periodId)
        .courseId(period.getCourseId())
        .memberId(userDetails.getId())
        .build().toEntity();
    log.debug("DTO 형태에서 attendance 엔티티 형태로 변환: {} ", attendance);

    attendance.updateStatus(status);
    log.info("status 업데이트");

    attendanceRepository.save(attendance);
    log.info("출석 등록을 완료합니다.");

  }

  private boolean isWithinNetwork(String targetIp) {
    // 학원 와이파이 네트워크 범위를 설정
    // 192.168.1.0/24 네트워크 범위를 사용하는 경우
    String[] allowedNetworks = {
        "127.0.0.1/32", // 로컬, 추가 네트워크 범위가 있을 경우 추가 가능
        "192.168.0.0/24", // 학원 네트워크(로컬네트워크)
        "115.93.9.236/30"  // 학원 와이파이 공인 ip
    };

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
      e.printStackTrace(); // 예외 처리
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

  private AttendanceStatus checkIfWithinTimeWindow(Period period) {
// 현재 시간
    LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));
    log.debug("현재 시간: {}", now);

//  교시 시작 시간
    LocalTime periodStartTime = period.getStartTime();
    log.debug("교시 시작 시간: {}", periodStartTime);

//    교시 종료 시간
    LocalTime periodEndTime = period.getEndTime();
    log.debug("교시 종료 시간: {}", periodEndTime);

    LocalTime allowedStart = periodStartTime.minusMinutes(5);
    log.debug("허용되는 시작 시간: {}", allowedStart);
    LocalTime allowedEnd = periodEndTime.plusMinutes(10);
    log.debug("허용되는 끝 시간: {}", allowedEnd);

    // 교시 시작 5분 전보다 더 이른 경우 예외 처리
    if (now.isBefore(allowedStart)) {
      throw new InvalidRequestException(ATTENDANCE_NOT_YET);
    }

    // 교시 시작 5분 전 ~ 교시 시작 10분 후 => 출석 가능
    if (now.isAfter(allowedStart) && now.isBefore(periodStartTime.plusMinutes(10))) {
      log.debug("출석 상태: PRESENT");
      return AttendanceStatus.PRESENT;
    }
    // 교시 시작 10분 후 ~ 교시 종료 전 => 지각
    if (now.isAfter(periodStartTime.plusMinutes(10)) && now.isBefore(periodEndTime)) {
      log.debug("출석 상태: LATE");
      return AttendanceStatus.LATE;
    }

    // 교시 종료 후 => 결석
    log.debug("출석 상태: ABSENT");
    return AttendanceStatus.ABSENT;

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
      log.info("요청한 사람의 Role:{}", role);
    } else if (role.equals("STUDENT")) {
      log.info("요청한 사람의 Role:{}", role);
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

    List<AttendanceTableDTO> attendanceMainDTOS = attendanceRepository.finAttendanceStatusByPeriods(
        userDetails.getId(), courseId, date, dayOfWeek);

    log.debug(attendanceMainDTOS.toString());

    return attendanceMainDTOS;
  }

  /*@Transactional
  public AttendDetailDTO getAttendanceDetail(CustomUserDetails userDetails, Long courseId,
      Long studentId, LocalDate date) {

    return null;

  }*/
}



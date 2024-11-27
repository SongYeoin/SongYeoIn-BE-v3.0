package com.syi.project.attendance.service;

import static com.syi.project.attendance.dto.response.AttendanceResponseDTO.fromEntity;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendListResponseDTO;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.repository.ScheduleRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
  public Page<AdminAttendListResponseDTO> getAllAttendancesForAdmin(CustomUserDetails userDetails,
      Long courseId, AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable) {
    /* 담당자는 courseId, studentId, date, member_name, attendance_status */

    log.info("전체 출석 조회");
    log.debug("courseId: {}, date: {}", courseId, dto.getDate());
    log.debug("필터링 조건 dto: studentName={}, status ={}",dto.getStudentName(), dto.getStatus());

    return attendanceRepository.findPagedAdminAttendListByCourseId(courseId, dto, pageable);

  }

  // 수강생
  // 출석 전체 조회
 public Page<AdminAttendListResponseDTO> getAllAttendancesForStudent(CustomUserDetails userDetails,
      Long courseId, AttendanceRequestDTO dto, Pageable pageable) {
    /* 수강생은 courseId, memberId , date(달, 일) */
    log.info("조건별로 출석 조회를 시도합니다.");
    log.debug("dto: memberId={}, courseId={} date={}", dto.getMemberId(), dto.getCourseId(),
        dto.getDate());
    log.debug("filter: name={}, status={}", dto.getStudentName(), dto.getStatus());

    log.info("자격 추출");
    // Role 추출
    String role = extractRole(userDetails);
    log.debug("사용자 Role: {}", role);

    Long adminId = userDetails.getId();
    log.debug("사용자 Id: {}", adminId);

    return attendanceRepository.findPagedStudentAttendListByCourseId(courseId, dto, pageable);

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
    List<Attendance> results = attendanceRepository.findAllAttendance(dto);

    if (results.isEmpty()) {
      log.warn("경고 : PeriodID {} 와 MemberID {} 로 조회한 결과가 없습니다.", dto.getPeriodId(),
          dto.getMemberId());
      throw new NoSuchElementException("조회된 출석이 없습니다.");
    }

    log.info("{} 개의 시간표 조회 중", results.size());

    // 조회한 결과 dto로 변환
    return fromEntity(results.get(0));

  }

  //  관리자
  /* 출석 수정 */
  @Transactional
  public void updateAttendance(Long attendanceId, AttendanceRequestDTO dto) {
    /* memberId와 attendanceId */
    log.info("{}에 대한 출석 상태를 수정합니다.", attendanceId);
    log.debug("출석 상태 수정 요청된 정보: {}", dto.getStatus());

    Attendance attendance = attendanceRepository.findById(attendanceId)
        .orElseThrow(() -> {
          log.error("에러: 출석 ID {} 에 대한 출석을 찾을 수 없습니다.", attendanceId);
          return new NoSuchElementException("Attendance not found with id " + attendanceId);
        });
    AttendanceStatus newStatus = AttendanceStatus.fromENStatus(dto.getStatus()); // 문자열을 Enum으로 변환
    attendance.updateStatus(newStatus);
    attendanceRepository.save(attendance);

    log.info("{}에 대한 출석 상태를 성공적으로 수정했습니다.", attendanceId);
  }

  //  관리자
  /* 결석 처리 */
  @Transactional
  public void updateAbsentStatus(AttendanceRequestDTO dto) {

    log.info(" 출석 상태를 결석으로 수정합니다.");

    // 어제 날짜
    LocalDate yesterday = LocalDate.now().minusDays(1);
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

  private void processStudentAttendance(LocalDate yesterday, Long courseId, Member student,
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

  private void enrollAbsentAttendance(LocalDate yesterday, Long courseId, Member student,
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
  public AttendanceResponseDTO createAttendance(AttendanceRequestDTO dto,
      HttpServletRequest request) {
    log.info("출석 등록을 시도합니다.");
    log.debug("출석 등록 요청된 정보: {}", dto);

    // 해당 교시가 현재 출석 가능 시간인지 확인
    log.info("교시 ID {} 를 조회합니다.", dto.getPeriodId());
    Optional<Period> period = periodRepository.findById(dto.getPeriodId());

    Attendance attendance = dto.toEntity();
    log.debug("출석 엔티티 변환 완료: {}", attendance);

    AttendanceStatus status;
    if (period.isPresent()) {
      status = checkIfWithinTimeWindow(period.get());
      attendance.updateStatus(status);
      log.info("출석 상태 필드 변경 status: {}", attendance.getStatus());
      log.debug("attendance: {}", attendance);

//      사용자의 IP 주소 확인
      String userIp = getClientIp(request); // 클라이언트 IP 가져오기
      log.info("사용자의 IP 주소: {}", userIp);

      if (!isWithinNetwork(userIp)) {
        log.error("User IP 가 허용된 범주 안에 있지 않습니다: {}",userIp);
        return AttendanceResponseDTO.withMessage("학원 네트워크에서만 출석이 가능합니다.");  //redirect로 변경
      }
    }

    Attendance savedAttendance = attendanceRepository.save(attendance);
    log.info("출석 등록을 완료합니다.");

    return fromEntity(savedAttendance);

  }

  private boolean isWithinNetwork(String targetIp) {
    // 학원 와이파이 네트워크 범위를 설정
    // 192.168.1.0/24 네트워크 범위를 사용하는 경우
    String[] allowedNetworks = {
        "127.0.0.1/32", // 로컬, 추가 네트워크 범위가 있을 경우 추가 가능
        "192.168.0.0/24" // 학원 네트워크
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
    LocalTime now = LocalTime.now();

//  교시 시작 시간
    LocalTime periodStartTime = period.getStartTime();

//    교시 종료 시간
    LocalTime periodEndTime = period.getEndTime();

    LocalTime start = periodStartTime.minusMinutes(5);
    LocalTime end = periodEndTime.plusMinutes(10);

    // 교시 시작 5분 전 ~ 교시 시작 10분 후 => 출석 가능
    if (now.isAfter(start) && now.isBefore(end)) {
      return AttendanceStatus.PRESENT;

      // 교시 시작 10분 후 ~ 종료 전 => 지각
    } else if (now.isAfter(end) && now.isBefore(periodEndTime)) {
      return AttendanceStatus.LATE;

      // 교시 종료 후 => 결석
    } else {
      return AttendanceStatus.ABSENT;

    }

  }

  //  관리자, 수강생
  /* 출석 상세 조회 */
  public List<AttendanceResponseDTO> findAttendanceById(AttendanceRequestDTO dto) {
    /* 교시번호와 수강생번호 */
    /* 수강생 정보, 시간표 정보, 출석 목록 정보 */

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
  }

  public String extractRole(@AuthenticationPrincipal CustomUserDetails userDetails) {
    return userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority) // Role 이름 추출
        .findFirst() // Role이 여러 개인 경우 첫 번째만 가져옴
        .orElseThrow(() -> new IllegalStateException("사용자 권한이 없습니다."));
  }

  public List<CourseListDTO> getAllCoursesByAdminId(CustomUserDetails userDetails) {
    return courseRepository.findCoursesByAdminId(userDetails.getId());

  }

  public AdminAttendDetailDTO getAttendanceDetail(CustomUserDetails userDetails, Long courseId, AttendanceRequestDTO dto) {
return null;

  }
}



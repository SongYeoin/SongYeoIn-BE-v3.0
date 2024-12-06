package com.syi.project.attendance.service;

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
import com.syi.project.attendance.exception.AttendanceNotYetException;
import com.syi.project.attendance.exception.NotInRangeException;
import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.e기
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
      throw new AttendanceNotYetException("교시 시작 5분 전보다 더 전에는 출석이 불가능합니다.");
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



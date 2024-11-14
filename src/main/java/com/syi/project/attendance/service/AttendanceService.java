package com.syi.project.attendance.service;

import static com.syi.project.attendance.dto.response.AttendanceResponseDTO.fromEntity;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.repository.ScheduleRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  // 수강생,담당자
  /* 출석 전체 조회 */
  public AttendanceResponseDTO getAllAttendances(AttendanceRequestDTO attendanceRequestDTO) {
    return null;
  }

  //  관리자
  /* 학생별 출석 조회 => 출석 전체 조회랑 같이 만들어질 확률 많음 */
  public AttendanceResponseDTO getAttendanceByMemberId(Long memberId) {
    return null;
  }

  //  관리자
  /* 출석 수정 */
  @Transactional
  public AttendanceResponseDTO updateAttendance(Long id,
      AttendanceRequestDTO attendanceRequestDTO) {
    return null;
  }

  //  관리자
  /* 결석 처리 */
  @Transactional
  public AttendanceResponseDTO updateAbsentStatus(AttendanceRequestDTO attendanceRequestDTO) {
    /* 체크박스로 id만 받아오기 */
    return null;
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

    String status;
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

  private String checkIfWithinTimeWindow(Period period) {
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
      return "출석";

      // 교시 시작 10분 후 ~ 종료 전 => 지각
    } else if (now.isAfter(end) && now.isBefore(periodEndTime)) {
      return "지각";

      // 교시 종료 후 => 결석
    } else {
      return "결석";
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
    List<Attendance> results = attendanceRepository.findAttendanceByIds(dto);

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
}

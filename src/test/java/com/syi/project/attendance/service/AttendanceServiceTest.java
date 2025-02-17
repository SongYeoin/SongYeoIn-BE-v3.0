package com.syi.project.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.auth.dto.AuthUserDTO;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.period.eneity.Period;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

  @Mock
  private AttendanceRepository attendanceRepository;

  @InjectMocks
  private AttendanceService attendanceService;

  private CustomUserDetails mockUser;
  private List<Period> periods;
  private Map<Long, AttendanceStatus> attendanceRecords;

  @BeforeEach
  void setUp() {
    mockUser = new CustomUserDetails(new AuthUserDTO(2L,"student01","학생일", Role.STUDENT));

    periods = Arrays.asList(
        new Period(1L, "1교시", LocalDateTime.of(2024, 6, 1, 9, 0).toLocalTime(), LocalDateTime.of(2024, 6, 1, 10, 0).toLocalTime()),
        new Period(2L, "2교시", LocalDateTime.of(2024, 6, 1, 10, 10).toLocalTime(), LocalDateTime.of(2024, 6, 1, 11, 10).toLocalTime()),
        new Period(3L, "3교시", LocalDateTime.of(2024, 6, 1, 11, 20).toLocalTime(), LocalDateTime.of(2024, 6, 1, 12, 20).toLocalTime())
    );

    // 출석 기록을 저장할 Map (교시 ID -> 출석 상태)
    attendanceRecords = new HashMap<>();
  }

  /*@Test
  void testHandleEnterAttendance() {
    List<LocalDateTime> testTimes = Arrays.asList(

        LocalDateTime.of(2024, 6, 1, 8, 10),  // 1교시 시작 40분보다 더 전 입실 (예외처리)
        LocalDateTime.of(2024, 6, 1, 8, 20),  // 1교시 시작 40분 전 입실 (정상)
        LocalDateTime.of(2024, 6, 1, 9, 10),  // 1교시 시작 후 입실 (정상)
        LocalDateTime.of(2024, 6, 1, 9, 30),  // 1교시 시작 후 20분 후 (지각)
        LocalDateTime.of(2024, 6, 1, 10, 5),  // 쉬는 시간에 입실 (2교시 출석, 1교시 결석)
        LocalDateTime.of(2024, 6, 1, 10, 15), // 2교시 시작 후 입실 (정상)
        LocalDateTime.of(2024, 6, 1, 10, 40), // 2교시 시작 후 20분 후 (지각)
        LocalDateTime.of(2024, 6, 1, 11, 15), // 쉬는 시간에 입실 (3교시 출석, 1,2교시 결석)
        LocalDateTime.of(2024, 6, 1, 11, 25), // 3교시 시작 후 입실 (정상)
        LocalDateTime.of(2024, 6, 1, 11, 50), // 3교시 시작 후 20분 후 (예외 발생)
        LocalDateTime.of(2024, 6, 1, 12, 30)  // 3교시 종료 후 입실 (예외 발생)
    );

    for (LocalDateTime enterDateTime : testTimes) {
      System.out.println("\n--- 테스트 시작: " + enterDateTime + " ---");

      try {
        when(attendanceRepository.existsByMemberIdAndDate(mockUser.getId(), enterDateTime.toLocalDate()))
            .thenReturn(false);

        when(attendanceRepository.findByMemberIdAndPeriodIdAndDate(anyLong(), anyLong(), any()))
            .thenAnswer(invocation -> {
              Long periodId = invocation.getArgument(1);
              return attendanceRecords.containsKey(periodId)
                  ? Optional.of(new Attendance(null, attendanceRecords.get(periodId), null, enterDateTime,enterDateTime, periodId, 100L, mockUser.getId(), null, enterDateTime, null))
                  : Optional.empty();
            });

        doAnswer(invocation -> {
          Attendance attendance = invocation.getArgument(0);
          attendanceRecords.put(attendance.getPeriodId(), attendance.getStatus());
          return null;
        }).when(attendanceRepository).save(any(Attendance.class));

        attendanceService.handleEnterAttendance(mockUser, periods, enterDateTime);
        System.out.println("✅ 정상 입실 처리 완료: " + enterDateTime);
        printAttendanceRecords();

      } catch (InvalidRequestException e) {
        System.out.println("❌ 예외 발생: " + e.getMessage() + " (" + enterDateTime + ")");
      }
    }
  }*/

  private void printAttendanceRecords() {
    System.out.println("📌 출석 기록:");
    for (Period period : periods) {
      AttendanceStatus status = attendanceRecords.getOrDefault(period.getId(), null);
      System.out.println("   - " + period.getName() + ": " + (status != null ? status : "미입력"));
    }
  }

}
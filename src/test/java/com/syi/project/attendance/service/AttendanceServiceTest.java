package com.syi.project.attendance.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.syi.project.attendance.repository.AttendanceRepository;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.period.entity.Period;
import com.syi.project.period.repository.PeriodRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

  @Mock
  private AttendanceRepository attendanceRepository;

  @Mock
  private PeriodRepository periodRepository;

  @Mock
  private CustomUserDetails mockUser;

  @InjectMocks
  private AttendanceService attendanceService;

  private List<Period> periods;
  private LocalDateTime now;
  private Map<Long, AttendanceStatus> attendanceRecords;
  private final Long courseId = 1L;
  private final Long earlyExitPeriodId = 2L;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this); // Mock 객체 초기화

    periods = Arrays.asList(
        new Period(1L, "1교시", LocalTime.of(9, 0), LocalTime.of(10, 0)),
        new Period(2L, "2교시", LocalTime.of(10, 10), LocalTime.of(11, 10)),
        new Period(3L, "3교시", LocalTime.of(11, 20), LocalTime.of(12, 20))
    );
    when(mockUser.getId()).thenReturn(100L);

    // 출석 기록을 저장할 Map (교시 ID -> 출석 상태)
    attendanceRecords = new HashMap<>();
  }

 /* @Test
  void testVariousExitTimes() {
    LocalDateTime enterTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 5));
    LocalDateTime[] exitTimes = {
        LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 30)), // 조퇴
        LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)),  // 정상 퇴실
        LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0))   // 너무 늦은 퇴실
    };


    // ✅ 초기에는 입실 기록이 없다고 가정
    when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(anyLong(), any(LocalDate.class)))
        .thenReturn(false);
//    when(attendanceRepository.existsByMemberIdAndDateAndExitTimeNotNull(anyLong(), any(LocalDate.class)))
//        .thenReturn(false);

    // ✅ 입실 처리
    for (Period period : periods) {
      Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, null, null, null, period.getId(), 101L, mockUser.getId(), null, null,enterTime );
      attendanceRecords.put(period.getId(), attendance.getStatus());
      when(attendanceRepository.findByMemberIdAndPeriodIdAndDate(mockUser.getId(), period.getId(), LocalDate.now()))
          .thenReturn(Optional.of(attendance));
    }

    when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
      Attendance savedAttendance = invocation.getArgument(0);
      attendanceRecords.put(savedAttendance.getPeriodId(), savedAttendance.getStatus());
      when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(anyLong(), any(LocalDate.class)))
          .thenReturn(true); // ⬅️ 입실 저장 후 존재 여부 true로 변경
      return savedAttendance;
    });

    // ✅ 입실 처리 수행 후, 입실 여부 확인을 `true`로 업데이트
    attendanceService.handleEnterAttendance(mockUser, periods, enterTime);
    when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(anyLong(), any(LocalDate.class)))
        .thenReturn(true); // ⬅️ 입실 후에는 반드시 true로 설정

    // ✅ 퇴실 시 예외가 발생하지 않아야 함
    assertDoesNotThrow(() -> attendanceService.handleExitAttendance(mockUser, periods, exitTimes[0]));

    System.out.println("✅ 입실 후 퇴실할 때 예외가 발생하지 않음");

//    // ✅ 퇴실 테스트
//    for (LocalDateTime exitTime : exitTimes) {
//      try {
//        attendanceService.handleExitAttendance(mockUser, periods, exitTime);
//      } catch (Exception e) {
//        System.out.println("퇴실 실패: " + e.getMessage());
//      }
//    }
//    printAttendanceRecords();
  }

  @Test
  void testExitAfterEntryRecord() {
    LocalDateTime enterTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 5));
    LocalDateTime exitDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));

    // ✅ 입실 전에는 false
    when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(anyLong(), any(LocalDate.class)))
        .thenReturn(false);

    // ✅ 입실 후에는 true로 변경
    when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
      Attendance savedAttendance = invocation.getArgument(0);

      // ✅ 저장 후 `findByMemberIdAndPeriodIdAndDate()`가 `Optional.of(savedAttendance)` 반환하도록 설정
      when(attendanceRepository.findByMemberIdAndPeriodIdAndDate(
          savedAttendance.getMemberId(), savedAttendance.getPeriodId(), LocalDate.now()))
          .thenReturn(Optional.of(savedAttendance));

      return savedAttendance;
    });

    // ✅ 입실 처리
    attendanceService.handleEnterAttendance(mockUser, periods, enterTime);

    boolean checkEntry = attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(mockUser.getId(), LocalDate.now());
    System.out.println("🚀 퇴실하기 전에 입실 기록이 있는지 확인: " + checkEntry);


    // ✅ save()가 정상적으로 호출되었는지 검증
    verify(attendanceRepository, times(1)).save(any(Attendance.class));

    // ✅ 퇴실 시 예외가 발생하지 않아야 함
    assertDoesNotThrow(() -> attendanceService.handleExitAttendance(mockUser, periods, exitDateTime));

    System.out.println("✅ 입실 후 퇴실할 때 예외가 발생하지 않음");
  }







  @Test
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
  }


  @Test
  void testHandleExitAttendance() {
    List<LocalDateTime> testTimes = Arrays.asList(
        LocalDateTime.of(2024, 6, 1, 12, 30), // 정상 퇴실 (3교시 종료 후 10분 이내)
        LocalDateTime.of(2024, 6, 1, 8, 0),   // 입실 안 한 상태에서 퇴실 (예외)
        LocalDateTime.of(2024, 6, 1, 12, 50), // 이미 퇴실한 상태에서 퇴실 (예외)
        LocalDateTime.of(2024, 6, 1, 7, 30)   // 퇴실 가능 시간 이전 (예외)
    );

    for (LocalDateTime exitDateTime : testTimes) {
      System.out.println("\n--- 퇴실 테스트 시작: " + exitDateTime + " ---");

      try {
        when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(mockUser.getId(), exitDateTime.toLocalDate()))
            .thenReturn(true); // 입실 기록 있음

        when(attendanceRepository.existsByMemberIdAndDateAndExitTimeNotNull(mockUser.getId(), exitDateTime.toLocalDate()))
            .thenReturn(false); // 아직 퇴실 안 함

        doAnswer(invocation -> {
          Attendance attendance = invocation.getArgument(0);
          attendanceRecords.put(attendance.getPeriodId(), attendance.getStatus());
          return null;
        }).when(attendanceRepository).save(any(Attendance.class));

        attendanceService.handleExitAttendance(mockUser, periods, exitDateTime);
        System.out.println("✅ 정상 퇴실 처리 완료: " + exitDateTime);
        printAttendanceRecords();

      } catch (InvalidRequestException e) {
        System.out.println("❌ 예외 발생: " + e.getMessage() + " (" + exitDateTime + ")");
      }
    }
  }

  @Test
  void testHandleEarlyLeaveAttendance() {
    List<LocalDateTime> testTimes = Arrays.asList(
        LocalDateTime.of(2024, 6, 1, 11, 30), // 정상 조퇴 (3교시 조퇴)
        LocalDateTime.of(2024, 6, 1, 8, 0),   // 입실 안 한 상태에서 조퇴 (예외)
        LocalDateTime.of(2024, 6, 1, 12, 50), // 이미 퇴실한 상태에서 조퇴 (예외)
        LocalDateTime.of(2024, 6, 1, 10, 0)   // 조퇴 가능 시간 이전 (예외)
    );

    for (LocalDateTime leaveDateTime : testTimes) {
      System.out.println("\n--- 조퇴 테스트 시작: " + leaveDateTime + " ---");

      try {
        when(attendanceRepository.existsByMemberIdAndDateAndEnterTimeNotNull(mockUser.getId(), leaveDateTime.toLocalDate()))
            .thenReturn(true); // 입실 기록 있음

        when(attendanceRepository.existsByMemberIdAndDateAndExitTimeNotNull(mockUser.getId(), leaveDateTime.toLocalDate()))
            .thenReturn(false); // 아직 퇴실 안 함

        doAnswer(invocation -> {
          Attendance attendance = invocation.getArgument(0);
          attendanceRecords.put(attendance.getPeriodId(), attendance.getStatus());
          return null;
        }).when(attendanceRepository).save(any(Attendance.class));

        Long leavePeriodId = 2L; // 2교시 조퇴 테스트
        attendanceService.handleEarlyExitAttendance(mockUser, periods, leaveDateTime, leavePeriodId);
        System.out.println("✅ 정상 조퇴 처리 완료: " + leaveDateTime);
        printAttendanceRecords();

      } catch (InvalidRequestException e) {
        System.out.println("❌ 예외 발생: " + e.getMessage() + " (" + leaveDateTime + ")");
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
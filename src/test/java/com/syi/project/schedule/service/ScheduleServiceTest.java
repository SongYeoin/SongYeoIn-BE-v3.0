/*
package com.syi.project.schedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.syi.project.period.dto.PeriodRequestDTO;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.dto.ScheduleRequestDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.entity.Schedule;
import com.syi.project.schedule.repository.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class ScheduleServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private PeriodRepository periodRepository;

  @InjectMocks
  private ScheduleService scheduleService;
  
  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void createSchedule() {
    // given
    PeriodRequestDTO period1 = PeriodRequestDTO.builder()
        .id(null)
        .dayOfWeek("월요일")
        .name("1교시")
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(10, 0))
        .build();

    PeriodRequestDTO period2 = PeriodRequestDTO.builder()
        .id(null)
        .dayOfWeek("화요일")
        .name("2교시")
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    ScheduleRequestDTO scheduleRequestDTO = ScheduleRequestDTO.builder()
        .id(null) // 새 스케줄 생성
        .enrollDate(LocalDate.now())
        .courseId(1L)
        .periods(List.of(period1, period2))
        .build();

    // Mock 설정: Schedule이 저장되었을 때 반환될 객체 설정
    Schedule scheduleEntity = scheduleRequestDTO.toEntity();
    ReflectionTestUtils.setField(scheduleEntity, "id", 1L);

    when(scheduleRepository.save(any(Schedule.class))).thenReturn(scheduleEntity);

    // Mock 설정: Period들이 저장되었을 때 반환될 리스트 설정
    Period periodEntity1 = scheduleRequestDTO.getPeriods().get(0).toEntity(scheduleRequestDTO.getCourseId(), 1L);
    Period periodEntity2 = scheduleRequestDTO.getPeriods().get(1).toEntity(scheduleRequestDTO.getCourseId(), 1L);
    List<Period> periodEntities = List.of(periodEntity1, periodEntity2);

    when(periodRepository.saveAll(anyList())).thenReturn(periodEntities);

    // when
    ScheduleResponseDTO responseDTO = scheduleService.createSchedule(scheduleRequestDTO);

    // then
    assertEquals(1L, responseDTO.getId());    //scheduleId 조회
    assertEquals(scheduleRequestDTO.getCourseId(), responseDTO.getCourseId());
    assertEquals(scheduleRequestDTO.getPeriods().size(), responseDTO.getPeriods().size());

    // 각 Period의 필드가 예상한 대로 매핑되었는지 검증
    assertEquals("월요일", responseDTO.getPeriods().get(0).getDayOfWeek());
    assertEquals(LocalTime.of(9, 0), responseDTO.getPeriods().get(0).getStartTime());

    assertEquals("화요일", responseDTO.getPeriods().get(1).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), responseDTO.getPeriods().get(1).getStartTime());

    // verify를 사용해 메서드 호출 횟수 검증
    verify(scheduleRepository, times(1)).save(any(Schedule.class));
    verify(periodRepository, times(1)).saveAll(anyList());

  }

  @Test
  void getScheduleById() {
    // given
    Schedule schedule = new Schedule(LocalDate.now(), null, null, 1L);
    ReflectionTestUtils.setField(schedule, "id", 1L);

    Period period1 = new Period(null,1L, 1L, "월요일", "1교시", LocalTime.of(9, 0), LocalTime.of(10, 0), null);
    Period period2 = new Period(null,1L, 1L, "화요일", "2교시", LocalTime.of(10, 0), LocalTime.of(11, 0), null);
    List<Period> periods = List.of(period1, period2);

    // Tuple 모킹을 위한 설정
    Tuple tuple1 = mock(Tuple.class);
    Tuple tuple2 = mock(Tuple.class);

    when(tuple1.get(0, Schedule.class)).thenReturn(schedule);
    when(tuple1.get(1, Period.class)).thenReturn(period1);

    when(tuple2.get(0, Schedule.class)).thenReturn(schedule);
    when(tuple2.get(1, Period.class)).thenReturn(period2);

    List<Tuple> mockResults = List.of(tuple1, tuple2);

    // Mock 설정: findScheduleWithPeriodsByCourseId 메서드가 mockResults를 반환하도록 설정
    //when(scheduleRepository.findScheduleWithPeriodsByCourseId(anyLong())).thenReturn(mockResults);

    // when
    ScheduleResponseDTO responseDTO = scheduleService.getScheduleById(1L);

    // then
    assertEquals(schedule.getId(), responseDTO.getId());
    assertEquals(schedule.getCourseId(), responseDTO.getCourseId());
    assertEquals(periods.size(), responseDTO.getPeriods().size());

    // 각 Period의 필드가 예상한 대로 매핑되었는지 확인
    assertEquals("월요일", responseDTO.getPeriods().get(0).getDayOfWeek());
    assertEquals(LocalTime.of(9, 0), responseDTO.getPeriods().get(0).getStartTime());

    assertEquals("화요일", responseDTO.getPeriods().get(1).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), responseDTO.getPeriods().get(1).getStartTime());

    // 메서드 호출 횟수 검증
    //verify(scheduleRepository, times(1)).findScheduleWithPeriodsByCourseId(anyLong());
  }

  @Test
  void updateSchedule() {
    // given
    Schedule schedule = new Schedule(LocalDate.now(), null, null, 1L);
    ReflectionTestUtils.setField(schedule, "id", 1L);

    Period period1 = new Period(1L,1L, 1L, "월요일", "1교시", LocalTime.of(9, 0), LocalTime.of(10, 0), null);
    Period period2 = new Period(2L,1L, 1L, "화요일", "2교시", LocalTime.of(10, 0), LocalTime.of(11, 0), null);
    List<Period> existingPeriods = List.of(period1, period2);

    PeriodRequestDTO periodDTO1 = PeriodRequestDTO.builder()
        .id(1L)
        .dayOfWeek("월요일")
        .name("1교시")
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(10, 0))
        .build();

    PeriodRequestDTO periodDTO2 = PeriodRequestDTO.builder()
        .id(2L)
        .dayOfWeek("화요일")
        .name("2교시")
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    ScheduleRequestDTO scheduleRequestDTO = ScheduleRequestDTO.builder()
        .id(1L)
        .courseId(1L)
        .periods(List.of(periodDTO1, periodDTO2))
        .build();

    when(scheduleRepository.findById(anyLong())).thenReturn(java.util.Optional.of(schedule));
    when(periodRepository.findPeriodsByScheduleIdForPatch(anyLong(), anyList())).thenReturn(existingPeriods);
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
    when(periodRepository.saveAll(anyList())).thenReturn(existingPeriods);

*/

    // when
    //ScheduleResponseDTO responseDTO = scheduleService.updateSchedule(1L, scheduleRequestDTO);

/*    // then
    // 검증: Schedule ID와 Course ID가 올바른지 확인
    assertEquals(schedule.getId(), responseDTO.getId());
    assertEquals(schedule.getCourseId(), responseDTO.getCourseId());
    assertEquals(existingPeriods.size(), responseDTO.getPeriods().size());

    // 각 Period의 필드가 예상한 대로 매핑되었는지 확인
    assertEquals("월요일", responseDTO.getPeriods().get(0).getDayOfWeek());
    assertEquals(LocalTime.of(9, 0), responseDTO.getPeriods().get(0).getStartTime());

    assertEquals("화요일", responseDTO.getPeriods().get(1).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), responseDTO.getPeriods().get(1).getStartTime());

    // 메서드 호출 횟수 검증
    verify(scheduleRepository, times(1)).findById(anyLong());
    verify(periodRepository, times(1)).findPeriodsByScheduleIdForPatch(anyLong(), anyList());
    verify(scheduleRepository, times(1)).save(any(Schedule.class));
    verify(periodRepository, times(1)).saveAll(anyList());
  }*/
/*
  @Test
  void deletePeriod() {
  }*/

//  private ScheduleRequestDTO scheduleRequestDTO;
//  private Schedule schedule;
//  private List<Period> periods;
//
//  @BeforeEach
//  void setUp() {
//    //MockitoAnnotations.openMocks(this);
//    mocks = MockitoAnnotations.openMocks(this);
//
//    // 기본 테스트 데이터 설정
//    schedule = new Schedule(LocalDate.now(), null, null, 1L);
//    ReflectionTestUtils.setField(schedule, "id", 1L);
//
//    PeriodRequestDTO periodDTO1 = new PeriodRequestDTO(1L, "월요일", "1교시", LocalTime.of(9, 0),
//        LocalTime.of(10, 0), null);
//    PeriodRequestDTO periodDTO2 = new PeriodRequestDTO(2L, "화요일", "2교시", LocalTime.of(10, 0),
//        LocalTime.of(11, 0), null);
//
//    scheduleRequestDTO = new ScheduleRequestDTO(1L, LocalDate.now(), null, null, 1L,
//        List.of(periodDTO1, periodDTO2));
//
//    Period period1 = periodDTO1.toEntity(scheduleRequestDTO.getCourseId(),
//        scheduleRequestDTO.getId());
//    Period period2 = periodDTO2.toEntity(scheduleRequestDTO.getCourseId(),
//        scheduleRequestDTO.getId());
//    periods = List.of(period1, period2);
//  }
//
//  @Test
//  void testCreateSchedule() {
//    // Schedule 엔티티 저장 시 mock 설정
//    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
//    when(periodRepository.saveAll(anyList())).thenReturn(periods);
//
//    // createSchedule 메서드 실행
//    ScheduleResponseDTO responseDTO = scheduleService.createSchedule(scheduleRequestDTO);
//
//    // 결과 검증
//    assertEquals(schedule.getId(), responseDTO.getId());
//    assertEquals(scheduleRequestDTO.getCourseId(), responseDTO.getCourseId());
//    assertEquals(periods.size(), responseDTO.getPeriods().size());
//
//    verify(scheduleRepository, times(1)).save(any(Schedule.class));
//    verify(periodRepository, times(1)).saveAll(anyList());
//  }
//
//  @Test
//  void testGetScheduleById() {
//    // Mock 데이터 설정
//    Tuple tuple1 = mock(Tuple.class);
//    Tuple tuple2 = mock(Tuple.class);
//    when(tuple1.get(0, Schedule.class)).thenReturn(schedule);
//    when(tuple1.get(1, Period.class)).thenReturn(periods.get(0));
//    when(tuple2.get(0, Schedule.class)).thenReturn(schedule);
//    when(tuple2.get(1, Period.class)).thenReturn(periods.get(1));
//
//    when(scheduleRepository.findScheduleWithPeriodsByCourseId(anyLong())).thenReturn(
//        List.of(tuple1, tuple2));
//
//    // 메서드 실행
//    ScheduleResponseDTO responseDTO = scheduleService.getScheduleById(
//        scheduleRequestDTO.getCourseId());
//
//    // 결과 검증
//    assertEquals(schedule.getId(), responseDTO.getId());
//    assertEquals(periods.size(), responseDTO.getPeriods().size());
//
//    verify(scheduleRepository, times(1)).findScheduleWithPeriodsByCourseId(anyLong());
//  }
//
//  @Test
//  void testUpdateSchedule() {
//    // Mock 데이터 설정
//    when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
//    when(periodRepository.findPeriodsByScheduleIdForPatch(anyLong(), anyList())).thenReturn(
//        periods);
//    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
//    when(periodRepository.saveAll(anyList())).thenReturn(periods);
//
//    // updateSchedule 메서드 실행
//    ScheduleResponseDTO responseDTO = scheduleService.updateSchedule(schedule.getId(),
//        scheduleRequestDTO);
//
//    // 결과 검증
//    assertEquals(schedule.getId(), responseDTO.getId());
//    assertEquals(scheduleRequestDTO.getCourseId(), responseDTO.getCourseId());
//
//    verify(scheduleRepository, times(1)).findById(anyLong());
//    verify(periodRepository, times(1)).findPeriodsByScheduleIdForPatch(anyLong(), anyList());
//    verify(scheduleRepository, times(1)).save(any(Schedule.class));
//    verify(periodRepository, times(1)).saveAll(anyList());
//  }
//
// /* @Test
//  void testDeletePeriod() {
//    // Mock 데이터 설정
//    Period periodToDelete = periods.get(0);
//    when(periodRepository.findById(anyLong())).thenReturn(Optional.of(periodToDelete));
//
//    // deletePeriod 메서드 실행
//    scheduleService.deletePeriod(periodToDelete.getId());
//
//    // 삭제 검증: findById만 호출 (실제 삭제 로직이 주석 처리된 경우)
//    verify(periodRepository, times(1)).findById(anyLong());
//  }*/
//
//  @Test
//  void testGetScheduleById_notFound() {
//    // 빈 리스트 반환으로 스케줄이 없을 때를 시뮬레이션
//    when(scheduleRepository.findScheduleWithPeriodsByCourseId(anyLong())).thenReturn(List.of());
//
//    // 예외 발생 확인
//    assertThrows(NoSuchElementException.class, () -> scheduleService.getScheduleById(1L));
//
//    verify(scheduleRepository, times(1)).findScheduleWithPeriodsByCourseId(anyLong());
//  }
//
//  @Test
//  void testUpdateSchedule_somePeriodsNotFound() {
//    // Mock 데이터 설정
//    when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
//    when(periodRepository.findPeriodsByScheduleIdForPatch(anyLong(), anyList())).thenReturn(
//        periods.subList(0, 1));
//
//    // 예외 발생 확인
//    assertThrows(
//        NoSuchElementException.class, () -> scheduleService.updateSchedule(1L, scheduleRequestDTO));
//
//    verify(scheduleRepository, times(1)).findById(anyLong());
//    verify(periodRepository, times(1)).findPeriodsByScheduleIdForPatch(anyLong(), anyList());
//  }
//}}
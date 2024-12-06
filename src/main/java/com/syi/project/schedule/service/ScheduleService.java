package com.syi.project.schedule.service;

import static com.syi.project.schedule.dto.ScheduleResponseDTO.fromEntity;

import com.syi.project.course.entity.Course;
import com.syi.project.period.DayOfWeekMapper;
import com.syi.project.period.dto.PeriodRequestDTO;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.dto.ScheduleRequestDTO;
import com.syi.project.schedule.dto.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO.ScheduleUpdateResponseDTO;
import com.syi.project.schedule.entity.Schedule;
import com.syi.project.schedule.repository.ScheduleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;

  private final PeriodRepository periodRepository;

  /* 시간표 등록 */
  @Transactional
  public ScheduleResponseDTO createSchedule(ScheduleRequestDTO scheduleDTO) {
    /* ScheduleId, courseId, List<Period> */

    log.info("Create Schedule with data : {}", scheduleDTO);

    Long scheduleId = scheduleDTO.getId();
    Schedule schedule;
    log.debug("scheduleId: {}", scheduleId);

    // 1. Schedule ID가 이미 존재하면 해당 스케줄을 가져오고, 없으면 새로운 스케줄 생성
    if (scheduleId != null) {
      schedule = scheduleRepository.findById(scheduleId)
          .orElseThrow(() -> {
            log.error("scheduleId {} schedule 을 찾을 수 없습니다.", scheduleDTO.getId());
            return new NoSuchElementException(
                "schedule 을 찾을 수 없습니다. scheduleId:" + scheduleDTO.getId());
          });
      log.info("scheduleId {} 가 이미 존재합니다", scheduleId);
      log.debug("조회된 schedule 정보: {}", schedule);
    } else {
      log.info("scheduleId가 존재하지 않으므로 생성합니다.");
      schedule = scheduleDTO.toEntity();  // courseId 만 가지고 entity 로 등록
      log.debug("scheduleDTO 을 Schedule 로 변환합니다. {}", schedule);

      try {
        log.debug("DB에 schedule을 저장하려고 시도 중입니다.");
        schedule = scheduleRepository.save(schedule);
        scheduleId = schedule.getId();  // 할당된 ID를 저장하여 이후에 사용
        log.info("시간표가 ID {} 번으로 성공적으로 등록되었습니다.", schedule.getId());
      } catch (Exception e) {
        log.error("시간표를 생성하는 도중에 에러가 발생했습니다.: {}", e.getMessage(), e);
        throw e;
      }

    }

    // 2. Period 생성 및 저장
    /*해당 교시가 이미 등록되어있는지 확인하는 과정 필요*/
    Long finalScheduleId = scheduleId;  // effectively final 로컬 변수로 할당
    List<Period> periods = null;
    if (scheduleDTO.getPeriods() != null) {
      periods = scheduleDTO.getPeriods().stream()
          .flatMap(periodDTO -> {
            // 요일 변환
            List<String> days = DayOfWeekMapper.mapToDays(periodDTO.getDayOfWeek());
            // 변환된 요일로 Period 엔티티 생성
            return days.stream()
                .map(day -> periodDTO.toEntity(scheduleDTO.getCourseId(), finalScheduleId, day));
          })
          .toList();
      periods = periodRepository.saveAll(periods);
      log.info("성공적으로 교시가 저장되었습니다. 저장된 데이터: {}", periods);
    }


    return fromEntity(schedule, periods);

  }


  /* 시간표 조회 */
  @Transactional
  public ScheduleResponseDTO getScheduleById(long courseId) {
    /* 1개의 반에는 1개의 시간표만 존재 */

    log.info("교육과정 ID {}에 대한 시간표를 처리 중입니다.", courseId);

    ScheduleResponseDTO results = scheduleRepository.findScheduleWithPeriodsByCourseId(courseId);
    log.info("변환된 scheduleResponseDTO: {}", results);

    return results;
  }

  /* 시간표 수정 */
  @Transactional
  public ScheduleUpdateResponseDTO updateSchedule(Course course, ScheduleUpdateRequestDTO request) {
    log.info("ID {} 번 시간표 수정", course.getScheduleId());
    log.debug("수정될 교시 정보: {}", request.getUpdatedPeriods());
    log.debug("새로 추가할 교시 정보: {}", request.getNewPeriods());
    log.debug("삭제될 교시 ID 리스트: {}", request.getDeletedPeriodIds());

    Long scheduleId = course.getScheduleId();

    // 1. ID로 Schedule, Period 조회, 없으면 예외 발생
    Schedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> {
          log.error("시간표 ID {}에 대한 정보를 찾을 수 없습니다.", scheduleId);
          return new NoSuchElementException("시간표를 찾을 수 없습니다. scheduleId: " + scheduleId);
        });

    List<PeriodRequestDTO> patchPeriodList = request.getUpdatedPeriods();
    log.debug("요청된 Period 목록: {}", patchPeriodList);

    List<Long> periodIdsToCheck = patchPeriodList.stream()
        .map(PeriodRequestDTO::getId)
        .toList();
    log.debug("요청된 Period ID 목록: {}", periodIdsToCheck);

    List<Period> existingPeriods = periodRepository.findPeriodsByScheduleIdForPatch(scheduleId,
        periodIdsToCheck);
    log.debug("존재하는 Period 목록: {}", existingPeriods);

    if (existingPeriods.size() != patchPeriodList.size()) {
      log.error("일부 Period ID가 존재하지 않습니다. 요청된 Period ID 목록: {}", periodIdsToCheck);
      throw new NoSuchElementException("일부 교시에 대한 정보를 찾을 수 없습니다.");
    }

    log.debug("시간표 수정되기 전 데이터: {}", schedule);
    log.debug("교시 수정되기 전 데이터: {}", existingPeriods);

    // 2. 필드 업데이트
    schedule.updateModifiedDate(LocalDate.now());
    log.info("schedule 필드 업데이트 완료");

    // Map으로 변환(빠르게 접근)
    Map<Long, Period> existingPeriodsMap = existingPeriods.stream()
        .collect(Collectors.toConcurrentMap(Period::getId, period -> period));  //키, value
    log.debug("existingPeriods 을 Map 형태로 변환");

    for (PeriodRequestDTO dto : patchPeriodList) {
      Period period = existingPeriodsMap.get(dto.getId());

      if (period == null) {
        log.error("Period ID {} 에 해당하는 Period를 찾을 수 없습니다.", dto.getId());
        throw new NoSuchElementException("Period ID " + dto.getId() + "에 해당하는 Period를 찾을 수 없습니다.");
      }
      period.updateWith(dto);
    }
    log.info("period 필드 업데이트 완료");

    // 3. 업데이트된 Schedule 저장
    Schedule updatedSchedule = scheduleRepository.save(schedule);
    log.info("시간표 ID: {} 성공적으로 수정했습니다.", scheduleId);
    log.debug("시간표 수정된 데이터: {}", updatedSchedule);

    List<Period> updatedPeriod = periodRepository.saveAll(existingPeriods);
    log.info("교시 성공적으로 수정했습니다.");
    log.debug("교시 수정된 데이터: {}", updatedPeriod);

    // 새롭게 등록하는 교시 저장하는 로직
    ScheduleResponseDTO createdSchedule = null;
    List<PeriodRequestDTO> newPeriods = request.getNewPeriods();
    if (newPeriods != null && !newPeriods.isEmpty()) {

      createdSchedule = createSchedule(
          new ScheduleRequestDTO(scheduleId, null, null, null, course.getId(), newPeriods));
    } else {
      log.info("새로 추가할 교시 정보가 없습니다.");
    }

    // 삭제할 교시 목록 있을 시(냅다 삭제)
    if (request.getDeletedPeriodIds() != null) {
      for (Long periodId : request.getDeletedPeriodIds()) {
        periodRepository.deleteById(periodId);
      }
    }

    // 4. 업데이트된 엔티티를 DTO로 변환하여 반환
    ScheduleResponseDTO updatedScheduleDTO = fromEntity(updatedSchedule, updatedPeriod);

    return ScheduleUpdateResponseDTO.builder()
        .scheduleId(scheduleId)
        .updatedPeriods(updatedScheduleDTO.getPeriods())
        .newPeriods(
            createdSchedule != null ? createdSchedule.getPeriods() : List.of()) // Null-safe 처리
        .build();
  }

  /* 시간표 삭제 */
  // 반 ID 얻어와서 해당하는 시간표 deletedBy 삭제
  @Transactional
  public void deletePeriod(Long memberId,Long courseId) {
    log.info("교시 삭제 By 교육 과정 ID: {} , 삭제자: {}", courseId, memberId);


    Schedule existingSchedule = scheduleRepository.findByCourseId(courseId);

    if(existingSchedule == null){
      log.error("Schedule not found with courseId: {}", courseId);
      throw new NoSuchElementException("Schedule not found with courseId: " + courseId);
    }


    List<Period> existingPeriod = periodRepository.findByCourseId(courseId);

    if(existingPeriod == null || existingPeriod.isEmpty()){
      log.error("Period not found with courseId: {}", courseId);
      throw new NoSuchElementException("Period not found with courseId " + courseId);
    }

    // 2. List<Period> 먼저 삭제하기
    for(Period period : existingPeriod){
      period.updateDeletedBy(memberId);
      log.info("periodId: {} deletedBy로 삭제", period.getId());
      // deletedBy로 삭제 완료
      periodRepository.save(period);
      log.info("periodId: {} 삭제 완료", period.getId());
    }

    log.info("ScheduleId: {} deletedBy로 삭제", existingSchedule.getId());
    existingSchedule.updateDeletedBy(memberId);
    scheduleRepository.save(existingSchedule);
    log.info("ScheduleId: {} 삭제 완료", existingSchedule.getId());

    log.info("Schedule,Period with courseId: {} deleted successfully", courseId);

  }

}

package com.syi.project.attendance.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.course.entity.QCourse.course;
import static com.syi.project.period.eneity.QPeriod.period;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.projection.AttendanceDailyStats;
import com.syi.project.attendance.dto.projection.QAttendanceDailyStats;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendancePrintResponseDto;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceStatusListDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceTableDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.MemberInfoInDetail;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.period.eneity.Period;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  public AttendanceRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  /*@Override
  public List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto) {
    return queryFactory.select(attendance)
        .from(attendance)
        .where(attendance.periodId.eq(dto.getPeriodId())
            .and(attendance.memberId.eq(dto.getMemberId()))
                .and(attendance.date.eq(dto.getDate())))
        .fetch();
  }

  @Override
  public List<Attendance> findAttendanceByPeriodAndMember(AttendanceRequestDTO dto) {
    return queryFactory.select(attendance)
        .from(attendance)
        .where(attendance.periodId.eq(dto.getPeriodId())
            .and(attendance.memberId.eq(dto.getMemberId()))
            .and(attendance.date.eq(dto.getDate())))
        .fetch();
  }
*/
  @Override
  public Page<AttendanceStatusListDTO> findAttendanceDetailByIds(Long courseId, Long studentId,
      LocalDate date, Pageable pageable) {

    log.debug("findAllAttendance : courseId={}, studentId: {}, date={} ",
        courseId, studentId, date);

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId)
        .and(attendance.memberId.eq(studentId))
        .and(attendance.date.eq(date)));

    List<Tuple> tuples  = queryFactory
        .select(
            period.dayOfWeek,
            period.name,
            period.startTime,
            period.endTime,
            attendance.id,
            attendance.enrollDate,
            attendance.status)
        .from(attendance)
        .join(period).on(attendance.periodId.eq(period.id))
        .where(predicate)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    log.debug("Query result size: {}", tuples.size());
    log.debug("쿼리 결과 튜플: {}", tuples);
    tuples.forEach(tuple -> {
      log.debug("Student ID: {}", tuple.get(member.id));
      log.debug("Status: {}", tuple.get(attendance.status)); // status 값 확인
    });

    // 데이터가 비어 있는 경우 로그 출력
    if (tuples.isEmpty()) {
      log.debug("조건에 맞는 데이터가 없습니다. courseId={}, studentId={}, date={}", courseId, studentId, date);
      return new PageImpl<>(Collections.emptyList(), pageable, 0L);
    }else {
      for (int i = 0; i < tuples.size(); i++) {
        log.debug("Tuple [{}]: {}", i, tuples.get(i));
      }
    }

    List<AttendanceStatusListDTO> content =tuples.stream()
        .map(tuple -> AttendanceStatusListDTO.builder()
            .periodName(Optional.ofNullable(tuple.get(period.name)).orElse("Unknown"))
            .startTime(tuple.get(period.startTime))
            .endTime(tuple.get(period.endTime))
            .attendanceId(tuple.get(attendance.id))
            .enrollDate(tuple.get(attendance.enrollDate))
            .status(Optional.ofNullable(tuple.get(attendance.status))
                .map(AttendanceStatus::toKorean) // null이 아닌 경우 한글 상태로 변환
                .orElse("Unknown"))
            .build())
        .toList();

    log.debug("변환된 List<AttendanceStatusListDTO>: {}",content);

    Long total = queryFactory
        .select(attendance.id.count())
        .from(attendance)
        .where(predicate)
        .fetchOne();

    // 데이터가 없어도 예외발생 하지 않도록 처리
    if (content.isEmpty()) {
      log.debug("no content 조건에 맞는 데이터가 없습니다.");
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public List<Attendance> findAttendanceByDateAndMemberId(LocalDate yesterday, Long id) {
    return queryFactory
        .selectFrom(attendance)
        .where(attendance.date.eq(yesterday)
            .and(attendance.memberId.eq(id)))
        .fetch();
  }

  @Override
  public Page<AttendListResponseDTO> findPagedAttendListByCourseId(Long courseId,
      AllAttendancesRequestDTO dto,List<String> periodNames, Pageable pageable) {
    log.info("queryDSL 실행 요청");
    log.debug("memberId: {}, courseId: {}, date: {}",dto.getStudentId(),courseId,dto.getDate());
    log.debug("필터링 요청 데이터: studentName={}, status={}, startDate={}, endDate={}",
        dto.getStudentName(), dto.getAttendanceStatus(), dto.getStartDate(), dto.getEndDate());


    // 1: 모든 Period 정보 조회 (해당 Course의 Period 정보만 가져옴)
    List<Period> periods = queryFactory
        .selectFrom(period)
        .where(period.courseId.eq(courseId))
        .orderBy(period.startTime.asc())
        .fetch();

    log.info("courseId {}에 해당하는 교시들 조회",courseId);
    log.debug("조회된 교시들 리스트: {}",periods);

    Page<AttendListResponseDTO> result;
    if (dto.getDate() != null) {
      result = findAdminAttendanceData(courseId, dto, periods, periodNames, pageable);
    } else {
      result = findStudentAttendanceData(courseId, dto, periods, periodNames, pageable);
    }

    return result;
  }

  /**
   * 학생 출석 데이터 조회 (기간 기준)
   */
  private Page<AttendListResponseDTO> findStudentAttendanceData(Long courseId, AllAttendancesRequestDTO dto, List<Period> periods, List<String> periodNames, Pageable pageable) {
    BooleanBuilder predicate = buildStudentPredicate(courseId, dto);

    log.debug("courseId: {}, dto: {}, periods: {}, periodNames: {}", courseId, dto, periods, periodNames);

    List<Tuple> tuples = queryFactory
        .select(
            attendance.memberId,
            attendance.date,
            attendance.periodId,
            attendance.status
        )
        .from(attendance)
        .where(predicate)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    log.info("학생이 조회한 출석 데이터");
    log.debug("학생-조회된 데이터: {}", tuples);

    List<AttendListResponseDTO> content = mapTuplesToDTO(tuples, periods, periodNames, false);

    Long total = queryFactory
        .select(attendance.id.count())
        .from(attendance)
        .where(predicate)
        .fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  /**
   * 관리자 출석 데이터 조회 (특정 날짜 기준)
   */
  private Page<AttendListResponseDTO> findAdminAttendanceData(Long courseId, AllAttendancesRequestDTO dto, List<Period> periods, List<String> periodNames, Pageable pageable) {
    BooleanBuilder predicate = buildAdminPredicate(courseId, dto);

    List<Tuple> tuples = queryFactory
        .select(
            member.id, member.name, course.name,
            attendance.date, attendance.periodId, attendance.status
        )
        .from(attendance)
        .join(member).on(attendance.memberId.eq(member.id)).fetchJoin()
        .join(course).on(attendance.courseId.eq(course.id)).fetchJoin()
        .where(predicate)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    log.info("관리자가 조회한 출석 데이터");
    log.debug("조회된 데이터: {}", tuples);

    List<AttendListResponseDTO> content = mapTuplesToDTO(tuples, periods, periodNames, true);

    // 데이터가 없어도 예외발생 하지 않도록 처리
    if (content.isEmpty()) {
      log.debug("조건에 맞는 데이터가 없습니다.");
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    Long total = queryFactory
        .select(attendance.id.count())
        .from(attendance)
        //.join(member).on(attendance.memberId.eq(member.id)) // ✅ 카운트 쿼리에서도 join 추가
        .where(predicate)
        .fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  /**
   * 학생용 BooleanBuilder 생성
   */
  private BooleanBuilder buildStudentPredicate(Long courseId, AllAttendancesRequestDTO dto) {
    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId));

    if (dto.getStartDate() != null) {
      predicate.and(attendance.date.goe(dto.getStartDate()));
    }
    if (dto.getEndDate() != null) {
      predicate.and(attendance.date.loe(dto.getEndDate()));
    }
    if (dto.getStudentId() != null) {
      predicate.and(attendance.memberId.eq(dto.getStudentId()));
    }
    if (dto.getAttendanceStatus() != null) {
      predicate.and(attendance.status.eq(dto.getAttendanceStatus()));
    }
    return predicate;
  }



  /**
   * 관리자용 BooleanBuilder 생성
   */
  private BooleanBuilder buildAdminPredicate(Long courseId, AllAttendancesRequestDTO dto) {
    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId));

    if (dto.getDate() != null) {
      predicate.and(attendance.date.eq(dto.getDate()));
    }
    if (!TextUtils.isBlank(dto.getStudentName())) {
      predicate.and(member.name.eq(dto.getStudentName()));
    }
    if (dto.getAttendanceStatus() != null) {
      predicate.and(attendance.status.eq(dto.getAttendanceStatus()));
    }
    return predicate;
  }

  /**
   * Tuple 데이터를 AttendListResponseDTO로 변환
   */
  private List<AttendListResponseDTO> mapTuplesToDTO(List<Tuple> tuples, List<Period> periods, List<String> periodNames, boolean isAdmin) {
    Map<Object, AttendListResponseDTO> attendanceMap = new HashMap<>();

    for (Tuple tuple : tuples) {
      AttendListResponseDTO responseDTO;
      if (isAdmin) {
        Long studentId = tuple.get(member.id);
        responseDTO = attendanceMap.computeIfAbsent(studentId, id -> AttendListResponseDTO.builder()
            .studentId((Long)id)
            .studentName(tuple.get(member.name))
            .courseName(tuple.get(course.name))
            .date(tuple.get(attendance.date))
            .students(new LinkedHashMap<>())
            .periods(periodNames)
            .build());
      } else {
        LocalDate date = tuple.get(attendance.date);
        Long studentId = tuple.get(attendance.memberId);
        responseDTO = attendanceMap.computeIfAbsent(date, d -> AttendListResponseDTO.builder()
            //.studentId(studentId)
            .date((LocalDate) d)
            .students(new LinkedHashMap<>())
            .periods(periodNames)
            .build());
      }

      log.debug("AttendListResponseDTO: {}", responseDTO);

      Long periodId = tuple.get(attendance.periodId);
      String status = tuple.get(attendance.status) != null
          ? tuple.get(attendance.status).toKorean()
          : "UNKNOWN";  // 기본값 설정


      String periodName = periods.stream()
          .filter(p -> p.getId().equals(periodId))
          .map(Period::getName)
          .findFirst()
          .orElse("Unknown");

      responseDTO.getStudents().put(periodName, status);
    }

    return new ArrayList<>(attendanceMap.values());
  }



  @Override
  public MemberInfoInDetail findMemberInfoByAttendance(Long courseId, Long studentId, LocalDate date) {
    log.info("수강생 정보를 표시하기 위한 쿼리 실행");
    log.debug("courseId:{}, studentId:{}, date:{}",courseId,studentId,date);

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId)
        .and(attendance.memberId.eq(studentId))
        .and(attendance.date.eq(date)));

    Tuple tuple = queryFactory
        .select(
            member.name,
            course.name,
            attendance.date,
            course.adminName)
        .distinct()
        .from(attendance)
        .join(member).on(attendance.memberId.eq(member.id))
        .join(course).on(attendance.courseId.eq(course.id))
        .where(predicate)
        .fetchOne();
    log.debug("조회된 결과: {}",tuple);

    if(tuple == null){
      log.warn("출석 상세보기 페이지에서 수강생 정보를 표시할 데이터가 없습니다.");
      return MemberInfoInDetail.builder().build();
    }

    return MemberInfoInDetail.builder()
        .studentName(tuple.get(member.name))
        .courseName(tuple.get(course.name))
        .date(tuple.get(attendance.date))
        .adminName(tuple.get(course.adminName))
        .build();
  }

  @Override
  public List<AttendanceTableDTO> findAttendanceStatusByPeriods(Long studentId, Long courseId,
      LocalDate date, String dayOfWeek) {
    log.debug("studentId: {}, courseId: {}, date: {}, dayOfWeek: {}", studentId, courseId, date,
        dayOfWeek);

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId)
        .and(attendance.date.eq(date))
        .and(attendance.memberId.eq(studentId)));

    // 오늘 날짜인지 확인하기
    LocalDate nowDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toInstant()
        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

    boolean isToday = date.isEqual(nowDate);

    // period 에서 id와 name 추출하기 위해 조회
    List<Period> periods = queryFactory
        .selectFrom(period)
        .where(period.courseId.eq(courseId).and(period.dayOfWeek.eq(dayOfWeek)))
        .orderBy(period.startTime.asc())
        .fetch();

    log.debug("{} 에 해당하는 교시들 모음: {}", dayOfWeek, periods);

    List<Tuple> tuples = queryFactory
        .select(
            attendance.periodId,  //periodId
            attendance.status,     //status
            attendance.enterTime,
            attendance.exitTime
        )
        .from(attendance)
        .where(predicate)
        .fetch();

    log.debug("출석 결과(List<Tuple>: {}", tuples.toString());

    // attendance 데이터를 Map으로 변환 (periodId -> status)
    Map<Long, String> attendanceMap = tuples.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(attendance.periodId),
            tuple -> tuple.get(attendance.status) != null ? tuple.get(attendance.status).toKorean()
                : null
        ));

    // attendance 데이터를 Map으로 변환 (periodId -> enterTime)
    Map<Long, LocalDateTime> enterTimeMap = tuples.stream()
        .filter(tuple -> tuple.get(attendance.enterTime) != null)  // null 값 제거
        .collect(Collectors.toMap(
            tuple -> tuple.get(attendance.periodId),
            tuple -> tuple.get(attendance.enterTime)
        ));

    // attendance 데이터를 Map으로 변환 (periodId -> exitTime)
    Map<Long, LocalDateTime> exitTimeMap = tuples.stream()
        .filter(tuple -> tuple.get(attendance.exitTime) != null)  // null 값 제거
        .collect(Collectors.toMap(
            tuple -> tuple.get(attendance.periodId),
            tuple -> tuple.get(attendance.exitTime)
        ));

// 결과 생성
    List<AttendanceTableDTO> results = periods.stream()
        .map(p -> {
          AttendanceTableDTO.AttendanceTableDTOBuilder builder = AttendanceTableDTO.builder()
              .periodId(p.getId())                                // 교시 ID
              .periodName(p.getName())                            // 교시명
              .status(attendanceMap.getOrDefault(p.getId(), null)); // 매칭되는 상태 또는 null

          if (isToday) { // 오늘이면 enterTime, exitTime 추가
            builder.enterTime(enterTimeMap.getOrDefault(p.getId(), null))
                .exitTime(exitTimeMap.getOrDefault(p.getId(), null));
          }

          return builder.build();
        })
        .toList();

    return results;

  }

  @Override
  public Optional<Attendance> findByMemberIdAndPeriodIdAndDate(Long memberId, Long periodId, LocalDate localDate) {
    return Optional.ofNullable(
        queryFactory.selectFrom(attendance)
            .where(attendance.memberId.eq(memberId).and(attendance.periodId.eq(periodId)).and(attendance.date.eq(localDate)))
            .fetchOne()
    );
  }

  @Override
  public List<AttendanceDailyStats> findAttendanceStatsByMemberAndCourse(Long memberId,
      Long courseId) {
    return queryFactory
        .select(new QAttendanceDailyStats(
            attendance.memberId,
            attendance.date,
            attendance.status.count().intValue(),
            attendance.status.when(AttendanceStatus.LATE).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.ABSENT).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.EARLY_LEAVE).then(1).otherwise(0).sum()
        ))
        .from(attendance)
        .where(attendance.memberId.eq(memberId)
            .and(attendance.courseId.eq(courseId)))
        .groupBy(attendance.date)
        .orderBy(attendance.date.asc())
        .fetch();
  }

  @Override
  public List<AttendanceDailyStats> findAttendanceStatsByCourse(Long courseId) {
    return queryFactory
        .select(new QAttendanceDailyStats(
            attendance.memberId,  // 학생 ID 추가
            attendance.date,
            attendance.status.count().intValue(),
            attendance.status.when(AttendanceStatus.LATE).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.ABSENT).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.EARLY_LEAVE).then(1).otherwise(0).sum()
        ))
        .from(attendance)
        .where(attendance.courseId.eq(courseId))
        .groupBy(attendance.memberId, attendance.date) // 학생별, 날짜별 그룹화
        .orderBy(attendance.memberId.asc(), attendance.date.asc())
        .fetch();
  }

  @Override
  public List<Tuple> findAttendanceStatusByStudentIdAndCourseIdAndDate(Long studentId, Long courseId,
      LocalDate date) {
    return queryFactory
        .select(attendance.status, period.name, period.id)
        .from(attendance)
        .join(period).on(attendance.periodId.eq(period.id))
        .where(
            attendance.memberId.eq(studentId),
            attendance.courseId.eq(courseId),
            attendance.date.eq(date)
        )
        .orderBy(period.startTime.asc())
        .fetch();
  }

  @Override
  public List<AttendanceDailyStats> findAttendanceStatsByStudentIdAndCourseIdAndDates(Long memberId,
      Long courseId, LocalDate startDate, LocalDate endDate) {
    return queryFactory
        .select(new QAttendanceDailyStats(
            attendance.memberId,
            attendance.date,
            attendance.status.count().intValue(),
            attendance.status.when(AttendanceStatus.LATE).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.ABSENT).then(1).otherwise(0).sum(),
            attendance.status.when(AttendanceStatus.EARLY_LEAVE).then(1).otherwise(0).sum()
        ))
        .from(attendance)
        .where(attendance.memberId.eq(memberId)
            .and(attendance.courseId.eq(courseId))
            .and(attendance.date.between(startDate,endDate)))
        .groupBy(attendance.memberId,attendance.date)
        .orderBy(attendance.date.asc())
        .fetch();
  }


}

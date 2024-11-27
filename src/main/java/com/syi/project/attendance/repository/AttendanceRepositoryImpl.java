package com.syi.project.attendance.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.course.entity.QCourse.course;
import static com.syi.project.period.eneity.QPeriod.period;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.AttendanceDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendListResponseDTO;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.period.eneity.Period;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  public List<Attendance> findAllAttendance(AttendanceRequestDTO dto) {

    log.debug("findAllAttendance : memberId={}, courseId: {}, periodId={}, date={} ",
        dto.getMemberId(), dto.getCourseId(), dto.getPeriodId(), dto.getDate());

    BooleanBuilder predicate = new BooleanBuilder();

    if (dto.getPeriodId() != null) {
      predicate.and(attendance.periodId.eq(dto.getPeriodId()));
    }
    if (dto.getMemberId() != null) {
      predicate.and(attendance.memberId.eq(dto.getMemberId()));
    }
    if (dto.getDate() != null) {
      predicate.and(attendance.date.eq(dto.getDate()));
    }
    if (dto.getCourseId() != null) {
      predicate.and(attendance.courseId.eq(dto.getCourseId()));
    }

    return queryFactory
        .selectFrom(attendance)
        .where(predicate)
        .fetch();
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
  public Page<AdminAttendListResponseDTO> findPagedAdminAttendListByCourseId(Long courseId,
      AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable) {
    log.info("queryDSL 실행 요청");
    log.debug("courseId: {}, date: {}",courseId,dto.getDate());
    log.debug("필터링 요청 데이터: studentName={}, status={} ",dto.getStudentName(), dto.getAttendanceStatus());

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId).and(attendance.date.eq(dto.getDate())));

    if(!TextUtils.isBlank(dto.getStudentName())){
      predicate.and(member.name.eq(dto.getStudentName()));
    }

    // 한글 status를 AttendanceStatus Enum으로 변환
    AttendanceStatus attendanceStatus = dto.getAttendanceStatus();
    if (attendanceStatus != null) {
      predicate.and(attendance.status.eq(attendanceStatus));
    }

    // 1: 모든 Period 정보 조회 (해당 Course의 Period 정보만 가져옴)
    List<Period> periods = queryFactory
        .selectFrom(period)
        .where(period.courseId.eq(courseId))
        .orderBy(period.startTime.asc())
        .fetch();

    log.info("courseId {}에 해당하는 교시들 조회",courseId);
    log.debug("조회된 교시들 리스트: {}",periods);

    /*List<Long> periodIds = queryFactory
        .select(period.id)
        .from(period)
        .where(period.courseId.eq(courseId))
        .fetch();*/

    // 2: 학생별 출석 데이터 조회
    List<Tuple> tuples = queryFactory
        .select(
            member.id,    //studentId
            member.name,  //studentName
            course.name,  //courseName
            attendance.date,  //date
            attendance.periodId,  //periodId
            attendance.status     //status
        )
        .from(attendance)
        .join(member).on(attendance.memberId.eq(member.id))
        .join(course).on(attendance.courseId.eq(course.id))
        .where(predicate)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    log.info("학생별 출석 데이터 조회");
    log.debug("조회된 출석 데이터: {}",tuples);


    Map<Long, AdminAttendListResponseDTO> attendanceMap = new HashMap<>();
    for (Tuple tuple : tuples) {
      Long studentId = tuple.get(member.id);
      log.debug("학생ID: {}",studentId);

      AdminAttendListResponseDTO responseDTO = attendanceMap.computeIfAbsent(studentId, id -> AdminAttendListResponseDTO.builder()
          .studentId(id)
          .studentName(tuple.get(member.name))
          .courseName(tuple.get(course.name))
          .date(tuple.get(attendance.date))
          .periods(new LinkedHashMap<>())
          .build());

      log.debug("DTO 형태의 studentId, studentName, courseName, date 가 매핑된 데이터");
      log.debug("AdminAttendListResponseDTO: {}",responseDTO);

      Long periodId = tuple.get(attendance.periodId);
      String status = Objects.requireNonNull(tuple.get(attendance.status)).toKorean();

      log.debug("조회된 결과에서 추출한 periodId={}, status={}",periodId,status);

      // Period Name 매핑
      String periodName = periods.stream()
          .filter(p -> p.getId().equals(periodId))
          .map(Period::getName)
          .findFirst()
          .orElse("Unknown");

      log.info("조회한 List<Period> 의 형태에서 periodName 추출하기");
      log.debug("periodName: {}",periodName);

      responseDTO.getPeriods().put(periodName,status);
      log.info("Map 형태로 <periodName, status> 저장");

    }
    log.info("Map<Long, AdminAttendListResponseDTO> 에서 AdminAttendListResponseDTO만 추출하기");
    List<AdminAttendListResponseDTO> content = new ArrayList<>(attendanceMap.values());
    log.debug("content: {}",content);

    // 데이터가 없어도 예외발생 하지 않도록 처리
    if (content.isEmpty()) {
      log.debug("조건에 맞는 데이터가 없습니다.");
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    // Step 3: Get the total count for pagination
    Long total = queryFactory
        .select(attendance.id.count())
        .from(attendance)
        .where(predicate)
        .fetchOne();
    log.info("페이징 처리를 위한 조건에 맞는 전체 출석의 개수");
    log.debug("total: {}",total);


    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<AdminAttendListResponseDTO> findPagedStudentAttendListByCourseId(Long courseId,
      AttendanceRequestDTO dto, Pageable pageable) {
    return null;
  }

}

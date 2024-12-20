package com.syi.project.attendance.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.course.entity.QCourse.course;
import static com.syi.project.period.eneity.QPeriod.period;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceStatusListDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceTableDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.MemberInfoInDetail;
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
  public Page<AttendListResponseDTO> findPagedAdminAttendListByCourseId(Long courseId,
      AllAttendancesRequestDTO dto,List<String> periodNames, Pageable pageable) {
    log.info("queryDSL 실행 요청");
    log.debug("courseId: {}, date: {}",courseId,dto.getDate());
    log.debug("필터링 요청 데이터: studentName={}, status={}, startDate={}, endDate={}",
        dto.getStudentName(), dto.getAttendanceStatus(), dto.getStartDate(), dto.getEndDate());

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId));

    if (dto.getDate() != null) {
      predicate.and(attendance.date.eq(dto.getDate()));   // 관리자
    } else {  //수강생
      if (dto.getStartDate() != null) {
        predicate.and(attendance.date.goe(dto.getStartDate()));
      }
      if (dto.getEndDate() != null) {
        predicate.and(attendance.date.loe(dto.getEndDate()));
      }
      if(dto.getStudentId() != null) {
        predicate.and(attendance.memberId.eq(dto.getStudentId()));
      }
    }

    // 필터링-관리자
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

    List<Tuple> tuples;
    if (dto.getDate() != null) {
      // 2: 학생별 출석 데이터 조회
      tuples = queryFactory
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

    } else {
      tuples = queryFactory
          .select(
              attendance.date,  //date
              attendance.periodId,  //periodId
              attendance.status     //status
          )
          .from(attendance)
          .where(predicate)
          .offset(pageable.getOffset())
          .limit(pageable.getPageSize())
          .fetch();

      log.info("출석 데이터 조회");

      log.debug("조회된 출석 데이터: {}",tuples);
    }

    Map<Object, AttendListResponseDTO> attendanceMap = new HashMap<>();
    for (Tuple tuple : tuples) {
      AttendListResponseDTO responseDTO;
      if(dto.getDate() != null){
        Long studentId = tuple.get(member.id);
        log.debug("학생ID: {}",studentId);

        responseDTO = attendanceMap.computeIfAbsent(studentId, id -> AttendListResponseDTO.builder()
            .studentId((Long) id)
            .studentName(tuple.get(member.name))
            .courseName(tuple.get(course.name))
            .date(tuple.get(attendance.date))
            .students(new LinkedHashMap<>())
            .periods(periodNames)
            .build());

        log.debug("DTO 형태의 studentId, studentName, courseName, date 가 매핑된 데이터");
      }else{
        LocalDate date = tuple.get(attendance.date); // 날짜
        log.debug("날짜: {}",date);
        responseDTO = attendanceMap.computeIfAbsent(date, d -> AttendListResponseDTO.builder()
            .date((LocalDate) d) // 날짜
            .students(new LinkedHashMap<>())
            .periods(periodNames)
            .build());

        log.debug("DTO 형태의 studentId, date 가 매핑된 데이터");
      }

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

      responseDTO.getStudents().put(periodName,status);
      log.info("Map 형태로 <periodName, status> 저장");

    }
    log.info("Map<Long, AdminAttendListResponseDTO> 에서 AdminAttendListResponseDTO만 추출하기");
    List<AttendListResponseDTO> content = new ArrayList<>(attendanceMap.values());
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
  public Page<AttendListResponseDTO> findPagedStudentAttendListByCourseId(Long courseId,
      AttendanceRequestDTO dto, Pageable pageable) {
    return null;
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
  public List<AttendanceTableDTO> finAttendanceStatusByPeriods(Long studentId, Long courseId,
      LocalDate date, String dayOfWeek) {
    log.debug("studentId: {}, courseId: {}, date: {}, dayOfWeek: {}", studentId, courseId, date,
        dayOfWeek);

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId)
        .and(attendance.date.eq(date))
        .and(attendance.memberId.eq(studentId)));

    // period 에서 id와 name 추출하기 위해 조회
    List<Period> periods = queryFactory
        .selectFrom(period)
        .where(period.courseId.eq(courseId).and(period.dayOfWeek.eq(dayOfWeek)))
        .orderBy(period.startTime.asc())
        .fetch();

    // Period 리스트에서 name 필드만 추출
    List<String> periodNames = periods.stream()
        .map(Period::getName) // Period 객체의 name 필드 추출
        .toList();

    log.debug("{} 에 해당하는 교시들 모음: {}", dayOfWeek, periods.toString());

    List<Tuple> tuples = queryFactory
        .select(
            attendance.periodId,  //periodId
            attendance.status     //status
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

// 결과 생성
    List<AttendanceTableDTO> results = periods.stream()
        .map(p -> AttendanceTableDTO.builder()
            .periodId(p.getId())                                // 교시 ID
            .periodName(p.getName())                            // 교시명
            .status(attendanceMap.getOrDefault(p.getId(), null)) // 매칭되는 상태 또는 null
            .build())
        .toList();

    return results;

  }


}

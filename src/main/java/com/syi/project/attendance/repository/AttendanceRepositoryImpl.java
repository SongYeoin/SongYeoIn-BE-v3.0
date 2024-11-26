package com.syi.project.attendance.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.course.entity.QCourse.course;
import static com.syi.project.period.eneity.QPeriod.period;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.AttendanceDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendList;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.entity.QAttendance;
import com.syi.project.common.enums.AttendanceStatus;
import com.syi.project.period.eneity.QPeriod;
import java.time.LocalDate;
import java.util.List;
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
  public Page<AdminAttendList> findPagedAdminAttendListByCourseId(Long courseId,
      AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable) {

    BooleanBuilder predicate = new BooleanBuilder(attendance.courseId.eq(courseId).and(attendance.date.eq(dto.getDate())));

    if(!TextUtils.isBlank(dto.getStudentName())){
      predicate.and(member.name.eq(dto.getStudentName()));
    }
    if(!TextUtils.isBlank(dto.getStatus())){
      predicate.and(attendance.status.eq(AttendanceStatus.valueOf(dto.getStatus())));
    }

    // Step 1: Fetch the basic fields for AdminAttendList
    List<Tuple> tuples = queryFactory
        .select(
            member.id.as("studentId"),
            member.name.as("studentName"),
            course.name.as("courseName"),
            attendance.date.as("date")
        )
        .from(attendance)
        .join(member).on(attendance.memberId.eq(member.id))
        .join(course).on(attendance.courseId.eq(course.id))
        .where(predicate)
        .groupBy(member.id, member.name, course.name, attendance.date)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

// Step 2: For each tuple, fetch the attendance list
    List<AdminAttendList> content = tuples.stream()
        .map(tuple -> {
          Long studentId = tuple.get(member.id);
          String studentName = tuple.get(member.name);
          String courseName = tuple.get(course.name);
          LocalDate attendanceDate = tuple.get(attendance.date);

          // Fetch the attendance list for this student and date
          List<AttendanceDTO> attendanceList = queryFactory
              .select(Projections.constructor(
                  AttendanceDTO.class,
                  period.name.as("periodName"),
                  attendance.status.as("status")
              ))
              .from(attendance)
              .join(period).on(attendance.periodId.eq(period.id))
              .fetch();

          // Build AdminAttendList
          return AdminAttendList.builder()
              .studentId(studentId)
              .studentName(studentName)
              .courseName(courseName)
              .date(attendanceDate)
              .attendanceList(attendanceList)
              .build();
        })
        .toList();

    // Step 3: Get the total count for pagination
    Long total = queryFactory
        .select(attendance.id.count())
        .from(attendance)
        .where(attendance.courseId.eq(courseId).and(attendance.date.eq(dto.getDate())))
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<AdminAttendList> findPagedStudentAttendListByCourseId(Long courseId,
      AttendanceRequestDTO dto, Pageable pageable) {
    return null;
  }

}

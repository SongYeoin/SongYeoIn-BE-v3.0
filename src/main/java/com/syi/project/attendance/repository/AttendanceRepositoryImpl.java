package com.syi.project.attendance.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.entity.Attendance;
import com.syi.project.attendance.entity.QAttendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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

}

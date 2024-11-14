package com.syi.project.attendance.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.entity.Attendance;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  public AttendanceRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto) {
    /*return queryFactory.select(attendance)
        .from(attendance)
        .where(attendance.periodId.eq(dto.getPeriodId())
            .and(attendance.memberId.eq(dto.getMemberId()))
                .and(attendance.date.eq(dto.getDate())))
        .fetch();*/
    return null;
  }
}

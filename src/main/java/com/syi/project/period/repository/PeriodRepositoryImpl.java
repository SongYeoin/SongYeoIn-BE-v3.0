package com.syi.project.period.repository;

import static com.syi.project.attendance.entity.QAttendance.attendance;
import static com.syi.project.period.entity.QPeriod.period;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.period.DayOrderUtil;
import com.syi.project.period.entity.Period;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class PeriodRepositoryImpl implements PeriodRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public PeriodRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public List<Period> findPeriodsByScheduleIdForPatch(Long scheduleId,
      List<Long> periodIdsToCheck) {
    return queryFactory.select(period)
        .from(period)
        .where(period.scheduleId.eq(scheduleId)
            .and(period.id.in(periodIdsToCheck))
            .and(period.deletedBy.isNull()))
        .fetch();
  }

  @Override
  public List<Period> getScheduleByCourseId(String dayOfWeek,Long courseId) {

    BooleanBuilder predicate = new BooleanBuilder(period.courseId.eq(courseId)
        .and(period.deletedBy.isNull()));

    if (StringUtils.hasText(dayOfWeek)) {
      predicate.and(period.dayOfWeek.eq(dayOfWeek.trim()));
    }

    return queryFactory.selectFrom(period)
        .where(predicate)
        .distinct()
        .orderBy(DayOrderUtil.getDayOrder(period.dayOfWeek),period.startTime.asc())
        .fetch();
  }

  /*@Override
  public List<Period> getScheduleByDayOfWeek(String dayOfWeekString) {

    List<Tuple> results = queryFactory.selectDistinct(
            period.dayOfWeek,
            period.scheduleId,
            period.name,
            period.id,
            schedule.courseId,
            period.startTime,
            period.endTime)
        .from(period)
        .join(schedule).on(period.scheduleId.eq(schedule.id))
        .where(period.dayOfWeek.eq(dayOfWeekString)
            .and(period.deletedBy.isNull())
            .and(schedule.deletedBy.isNull()))
        .orderBy(DayOrderUtil.getDayOrder(period.dayOfWeek),period.startTime.asc())
        .fetch();

    return results.stream()
        .map(tuple -> new Period(
            tuple.get(period.id),
            tuple.get(period.courseId),
            tuple.get(period.scheduleId),
            tuple.get(period.dayOfWeek),
            tuple.get(period.name),
            tuple.get(period.startTime),
            tuple.get(period.endTime),
            null
        )).toList();

  }*/

  @Override
  public List<Period> findByCourseId(Long courseId) {
    return queryFactory.selectFrom(period)
        .where(period.courseId.eq(courseId)
            .and(period.deletedBy.isNull()))
        .orderBy(period.startTime.asc())
        .fetch();
  }

  // 관리자: 요일에 해당하는 교시 중복을 제거해서 가지고 오기
  @Override
  public List<String> findPeriodsByDayOfWeek(Long courseId, String dayOfWeek) {
    List<Tuple> periods =  queryFactory
        .select(period.name, period.startTime)
        .distinct()
        .from(period)
        .where(period.dayOfWeek.eq(dayOfWeek)
            .and(period.courseId.eq(courseId))
            .and(period.deletedBy.isNull()))
        .orderBy(period.startTime.asc()) // 시간 순으로 정렬
        .fetch();

    return periods.stream()
        .map(tuple -> tuple.get(period.name))
        .distinct()
        .toList();
  }

  // 수강생: 시작날짜와 끝날짜 사이에 존재하는 교시명 중복을 제거해서 가지고 오기
  @Override
  public List<String> findPeriodsInRange(Long courseId, LocalDate startDate, LocalDate endDate) {
    List<Tuple> periods = queryFactory
        .select(period.name, period.startTime)
        .distinct()
        .from(attendance)
        .join(period).on(attendance.periodId.eq(period.id))
        .where(attendance.date.between(startDate, endDate)
            .and(period.courseId.eq(courseId))
            .and(period.deletedBy.isNull()))
        .orderBy(period.startTime.asc())
        .fetch();

// 필요한 경우 `period.name`만 추출
    return periods.stream()
        .map(tuple -> tuple.get(period.name))
        .distinct()
        .toList();
  }
}

package com.syi.project.period.repository;

import static com.syi.project.period.eneity.QPeriod.period;
import static com.syi.project.schedule.entity.QSchedule.schedule;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.period.eneity.Period;
import java.util.List;
import org.springframework.stereotype.Repository;

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
  public List<Period> getScheduleByDayOfWeek(String dayOfWeekString) {

    List<Tuple> results = queryFactory.selectDistinct(
            period.dayOfWeek,
            period.scheduleId,
            period.name,
            period.id,
            schedule.courseId)
        .from(period)
        .join(schedule).on(period.scheduleId.eq(schedule.id))
        .where(period.dayOfWeek.containsIgnoreCase(dayOfWeekString)
            .and(period.deletedBy.isNull())
            .and(schedule.deletedBy.isNull()))
        .orderBy(period.scheduleId.asc())
        .fetch();

    return results.stream()
        .map(tuple -> new Period(
            tuple.get(period.id),
            tuple.get(period.courseId),
            tuple.get(period.scheduleId),
            tuple.get(period.dayOfWeek),
            tuple.get(period.name),
            null,
            null,
            null
        )).toList();

  }

  @Override
  public List<Period> findByCourseId(Long courseId) {
    return queryFactory.selectFrom(period)
        .where(period.courseId.eq(courseId)
            .and(period.deletedBy.isNull()))
        .fetch();
  }
}

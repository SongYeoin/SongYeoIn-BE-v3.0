package com.syi.project.schedule.repository;

import static com.syi.project.period.eneity.QPeriod.period;
import static com.syi.project.schedule.entity.QSchedule.schedule;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ScheduleRepositoryCustomImpl implements ScheduleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public ScheduleRepositoryCustomImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public List<Tuple> findScheduleWithPeriodsByCourseId(Long courseId) {
    return queryFactory.select(schedule, period)
        .from(schedule)
        .leftJoin(period).on(period.scheduleId.eq(schedule.id))
        .where(schedule.courseId.eq(courseId))
        .fetch();
  }


}

package com.syi.project.period.repository;

import static com.syi.project.period.eneity.QPeriod.period;

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
            .and(period.id.in(periodIdsToCheck)))
        .fetch();
  }
}

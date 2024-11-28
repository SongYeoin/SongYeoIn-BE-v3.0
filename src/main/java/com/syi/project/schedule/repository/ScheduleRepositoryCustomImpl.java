package com.syi.project.schedule.repository;

import static com.syi.project.period.eneity.QPeriod.period;
import static com.syi.project.schedule.entity.QSchedule.schedule;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.period.eneity.Period;
import com.syi.project.period.eneity.QPeriod;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.entity.QSchedule;
import com.syi.project.schedule.entity.Schedule;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ScheduleRepositoryCustomImpl implements ScheduleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public ScheduleRepositoryCustomImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public ScheduleResponseDTO findScheduleWithPeriodsByCourseId(Long courseId) {
/*    return queryFactory.select(schedule, period)
        .from(schedule)
        .leftJoin(period).on(period.scheduleId.eq(schedule.id))
        .where(schedule.courseId.eq(courseId))
        .fetch();*/

    NumberExpression<Integer> dayOrder = new CaseBuilder()
        .when(period.dayOfWeek.eq("월요일")).then(1)
        .when(period.dayOfWeek.eq("화요일")).then(2)
        .when(period.dayOfWeek.eq("수요일")).then(3)
        .when(period.dayOfWeek.eq("목요일")).then(4)
        .when(period.dayOfWeek.eq("금요일")).then(5)
        .when(period.dayOfWeek.eq("토요일")).then(6)
        .otherwise(7);

    OrderSpecifier<Integer> orderSpecifier = new OrderSpecifier<>(Order.ASC, dayOrder);

    List<Tuple> scheduleWithPeriods = queryFactory
        .select(schedule, period)
        .from(schedule)
        .leftJoin(period).on(period.scheduleId.eq(schedule.id))
        .where(schedule.courseId.eq(courseId))
        .orderBy(orderSpecifier,period.startTime.asc())
        .fetch();

    if (scheduleWithPeriods.isEmpty() || scheduleWithPeriods.get(0).get(QSchedule.schedule) == null) {
      //throw new NoSuchElementException("Schedule not found for courseId: " + courseId);
      log.warn("경고 : 교육과정 ID {}에 대한 시간표가 비어있습니다.", courseId);
      return ScheduleResponseDTO.builder()
          .id(null)
          .courseId(courseId)
          .periods(Collections.emptyList()).build();
    }

    // Schedule
    Schedule schedule = scheduleWithPeriods.get(0).get(QSchedule.schedule);

    // Period 리스트
    List<Period> periodList = scheduleWithPeriods.stream()
        .map(tuple -> tuple.get(period))
        .filter(Objects::nonNull)
        .toList();

    // ScheduleResponseDTO 생성
    return ScheduleResponseDTO.fromEntity(schedule, periodList);

  }

  @Override
  public Schedule findByCourseId(Long courseId) {
    return queryFactory.selectFrom(schedule)
        .where(schedule.courseId.eq(courseId)
            .and(schedule.deletedBy.isNull()))
        .fetchOne();
  }


}

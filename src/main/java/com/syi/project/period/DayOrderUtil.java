package com.syi.project.period;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

public class DayOrderUtil {
  /**
   * 요일 순서를 정의하는 메서드
   *
   * @param dayOfWeekExpression 요일 표현식 (예: period.dayOfWeek)
   * @return OrderSpecifier 객체 (ASC 기준)
   */
  public static OrderSpecifier<Integer> getDayOrder(StringExpression dayOfWeekExpression) {
    NumberExpression<Integer> dayOrder = new CaseBuilder()
        .when(dayOfWeekExpression.eq("월요일")).then(1)
        .when(dayOfWeekExpression.eq("화요일")).then(2)
        .when(dayOfWeekExpression.eq("수요일")).then(3)
        .when(dayOfWeekExpression.eq("목요일")).then(4)
        .when(dayOfWeekExpression.eq("금요일")).then(5)
        .when(dayOfWeekExpression.eq("토요일")).then(6)
        .otherwise(7); // "일요일" 또는 기타 값

    return new OrderSpecifier<>(Order.ASC, dayOrder);
  }
}

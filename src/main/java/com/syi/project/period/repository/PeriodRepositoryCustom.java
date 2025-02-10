package com.syi.project.period.repository;

import com.syi.project.period.eneity.Period;
import java.time.LocalDate;
import java.util.List;

public interface PeriodRepositoryCustom {

  List<Period> findPeriodsByScheduleIdForPatch(Long scheduleId, List<Long> periodIdsToCheck);

  List<Period> getScheduleByCourseId(String dayOfWeek,Long courseId);

  List<Period> findByCourseId(Long courseId);

  List<String> findPeriodsByDayOfWeek(Long courseId, String dayOfWeek);
  List<String> findPeriodsInRange(Long courseId, LocalDate start, LocalDate end);
}

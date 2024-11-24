package com.syi.project.period.repository;

import com.syi.project.period.eneity.Period;
import java.util.List;
import java.util.Optional;

public interface PeriodRepositoryCustom {

  List<Period> findPeriodsByScheduleIdForPatch(Long scheduleId, List<Long> periodIdsToCheck);

  List<Period> getScheduleByDayOfWeek(String dayOfWeekString);

  List<Period> findByCourseId(Long courseId);
}

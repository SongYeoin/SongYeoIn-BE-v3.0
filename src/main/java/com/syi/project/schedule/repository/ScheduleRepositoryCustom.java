package com.syi.project.schedule.repository;

import com.querydsl.core.Tuple;
import java.util.List;

public interface ScheduleRepositoryCustom {

  List<Tuple> findScheduleWithPeriodsByCourseId(Long courseId);

}

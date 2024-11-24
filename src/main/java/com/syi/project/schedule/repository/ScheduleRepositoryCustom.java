package com.syi.project.schedule.repository;

import com.querydsl.core.Tuple;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import java.util.List;

public interface ScheduleRepositoryCustom {

  //List<Tuple> findScheduleWithPeriodsByCourseId(Long courseId);
  ScheduleResponseDTO findScheduleWithPeriodsByCourseId(Long courseId);

}

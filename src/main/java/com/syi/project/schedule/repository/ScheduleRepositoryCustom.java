package com.syi.project.schedule.repository;

import com.querydsl.core.Tuple;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.entity.Schedule;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepositoryCustom {

  //List<Tuple> findScheduleWithPeriodsByCourseId(Long courseId);
  ScheduleResponseDTO findScheduleWithPeriodsByCourseId(Long courseId);

  Schedule findByCourseId(Long courseId);
}

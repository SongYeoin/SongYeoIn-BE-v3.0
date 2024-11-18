package com.syi.project.schedule.repository;

import com.syi.project.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ScheduleRepository extends JpaRepository<Schedule, Long>,
    ScheduleRepositoryCustom {
  //QuerydslPredicateExecutor<Schedule>

}

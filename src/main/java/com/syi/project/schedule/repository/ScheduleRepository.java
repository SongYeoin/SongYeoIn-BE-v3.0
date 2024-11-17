package com.syi.project.schedule.repository;

import com.syi.project.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


public interface ScheduleRepository extends JpaRepository<Schedule, Long>,
    ScheduleRepositoryCustom {
  //QuerydslPredicateExecutor<Schedule>

}

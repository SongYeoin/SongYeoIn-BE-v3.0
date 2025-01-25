package com.syi.project.attendance.repository;

import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>,AttendanceRepositoryCustom {

  boolean existsByMemberIdAndDate(Long id, LocalDate localDate);

  boolean existsByMemberIdAndDateAndExitTimeNotNull(Long id, LocalDate localDate);

}

package com.syi.project.attendance.repository;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>,AttendanceRepositoryCustom {

}

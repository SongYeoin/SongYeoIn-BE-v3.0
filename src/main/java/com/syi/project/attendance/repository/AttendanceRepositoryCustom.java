package com.syi.project.attendance.repository;

import com.querydsl.core.Tuple;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.entity.Attendance;
import java.util.List;

public interface AttendanceRepositoryCustom {
  List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto);
}

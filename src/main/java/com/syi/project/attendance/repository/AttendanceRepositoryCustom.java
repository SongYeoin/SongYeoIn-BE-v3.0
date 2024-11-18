package com.syi.project.attendance.repository;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepositoryCustom {
/*  List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByPeriodAndMember(AttendanceRequestDTO dto);*/

  List<Attendance> findAllAttendance(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByDateAndMemberId(LocalDate yesterday, Long id);
}

package com.syi.project.attendance.repository;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendList;
import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceRepositoryCustom {
/*  List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByPeriodAndMember(AttendanceRequestDTO dto);*/

  List<Attendance> findAllAttendance(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByDateAndMemberId(LocalDate yesterday, Long id);

  Page<AdminAttendList> findPagedAdminAttendListByCourseId(Long courseId, AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable);

  Page<AdminAttendList> findPagedStudentAttendListByCourseId(Long courseId, AttendanceRequestDTO dto, Pageable pageable);
}

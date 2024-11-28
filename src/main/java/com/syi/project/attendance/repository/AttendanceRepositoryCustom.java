package com.syi.project.attendance.repository;

import com.querydsl.core.Tuple;
import com.syi.project.attendance.dto.AttendanceDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceStatusListDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.MemberInfoInDetail;
import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceRepositoryCustom {
/*  List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByPeriodAndMember(AttendanceRequestDTO dto);*/

  Page<AttendanceStatusListDTO> findAttendanceDetailByIds(Long courseId, Long studentId, LocalDate date, Pageable pageable);

  List<Attendance> findAttendanceByDateAndMemberId(LocalDate yesterday, Long id);

  Page<AdminAttendListResponseDTO> findPagedAdminAttendListByCourseId(Long courseId, AttendanceRequestDTO.AllAttendancesRequestDTO dto, Pageable pageable);

  Page<AdminAttendListResponseDTO> findPagedStudentAttendListByCourseId(Long courseId, AttendanceRequestDTO dto, Pageable pageable);

  MemberInfoInDetail findMemberInfoByAttendance(Long courseId, Long studentId, LocalDate date);
}

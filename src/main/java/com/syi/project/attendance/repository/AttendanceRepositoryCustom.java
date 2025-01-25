package com.syi.project.attendance.repository;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceStatusListDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceTableDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.MemberInfoInDetail;
import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceRepositoryCustom {
/*  List<Attendance> findAttendanceByIds(AttendanceRequestDTO dto);

  List<Attendance> findAttendanceByPeriodAndMember(AttendanceRequestDTO dto);*/

  Page<AttendanceStatusListDTO> findAttendanceDetailByIds(Long courseId, Long studentId, LocalDate date, Pageable pageable);

  List<Attendance> findAttendanceByDateAndMemberId(LocalDate yesterday, Long id);

  Page<AttendListResponseDTO> findPagedAdminAttendListByCourseId(Long courseId,
      AllAttendancesRequestDTO dto, List<String> periods, Pageable pageable);

  Page<AttendListResponseDTO> findPagedStudentAttendListByCourseId(Long courseId,
      AttendanceRequestDTO dto, Pageable pageable);

  MemberInfoInDetail findMemberInfoByAttendance(Long courseId, Long studentId, LocalDate date);

  List<AttendanceTableDTO> findAttendanceStatusByPeriods(Long id, Long courseId, LocalDate date,
      String dayOfWeek);

 Optional<Attendance> findByMemberIdAndPeriodIdAndDate(Long memberId, Long periodId,
     LocalDate localDate);
}

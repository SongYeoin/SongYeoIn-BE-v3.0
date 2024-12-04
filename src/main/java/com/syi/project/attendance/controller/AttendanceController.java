package com.syi.project.attendance.controller;

import
    com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.StudentAllAttendRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceTotalResponseDTO;
import com.syi.project.attendance.service.AttendanceService;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.MemberService;
import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.service.CourseService;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "attendance", description = "출석 API")
public class AttendanceController {

  private final AttendanceService attendanceService;
  private final CourseService courseService;
  private final ScheduleService scheduleService;
  //private final EnrollService enrollService;
  private final MemberService memberService;

  // 출석 등록
  @Operation(summary = "출석 등록", description = "출석을 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "출석을 성공적으로 등록했습니다."),
      })
  @PostMapping("/enroll")
  public ResponseEntity<AttendanceResponseDTO> createAttendance(
      @RequestBody @Valid AttendanceRequestDTO attendanceRequestDTO, HttpServletRequest request) {
    log.info("출석 등록 요청");
    log.info("등록 요청된 attendanceRequestDTO: {}", attendanceRequestDTO);
    AttendanceResponseDTO createdAttendance = attendanceService.createAttendance(
        attendanceRequestDTO, request);
    log.info("출석 등록된 정보: {}", createdAttendance);
    return ResponseEntity.ok(createdAttendance);
  }

  // 출석 전체 조회_수강생(일주일단위로)
  @GetMapping("course/{courseId}")
  public ResponseEntity<Page<AttendListResponseDTO>> getAllAttendance(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @RequestParam(required = false) String status,
      @PageableDefault(size = 10) Pageable pageable) {

    log.info("수강생 출석 전체 조회 요청");
    log.debug("출석 조회 요청 자격: {}, PK: {}", userDetails.getAuthorities(), userDetails.getId());
    log.info("startDate: {}, endDate: {},  status: {}", startDate, endDate, status);

    StudentAllAttendRequestDTO requestDTO = new StudentAllAttendRequestDTO(
        startDate, endDate, status);

    Page<AttendListResponseDTO> attendances = attendanceService.getAllAttendancesForStudent(
        userDetails, courseId,
        requestDTO, pageable);

    log.info("조회된 출석 정보: {} ", attendances.getContent());
    return ResponseEntity.ok(attendances);
  }

  //  출석 상세 조회_수강생
  @GetMapping("{id}")
  public ResponseEntity<AttendanceTotalResponseDTO> getAttendanceById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @PathVariable Long studentId,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    log.info("출석 상세 조회 요청");
    //log.info("상세 조회 요청된 attendanceRequestDTO: {}", attendanceRequestDTO);
//
//    /* 출석 목록 조회 */
//    List<AttendanceResponseDTO> attendanceInfo = attendanceService.findAttendanceByIds(
//        userDetails, courseId,
//        studentId, date);
//    log.info("출석 상세 조회 완료 {}", attendanceInfo.size());
//
//    /* 시간표 정보 조회 */
//    ScheduleResponseDTO scheduleInfo = scheduleService.getScheduleById(
//        attendanceRequestDTO.getCourseId());
//
//    AttendanceTotalResponseDTO responseDTO = AttendanceTotalResponseDTO.builder()
//        .attendanceInfo(attendanceInfo)
//        .scheduleInfo(scheduleInfo)
//        .build();
//    return ResponseEntity.ok(responseDTO);
    return null;
  }

  @GetMapping("/courses")
  public ResponseEntity<List<CourseListDTO>> getAllCoursesByAdminId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("adminId: {} 가 맡고 있는 교육과정 조회 요청", userDetails.getId());
    List<CourseDTO.CourseListDTO> courses = attendanceService.getAllCoursesByStudentId(userDetails);
    log.debug("총 {} 개의 교육과정 조회 완료", courses.size());
    return ResponseEntity.ok(courses);
  }



}

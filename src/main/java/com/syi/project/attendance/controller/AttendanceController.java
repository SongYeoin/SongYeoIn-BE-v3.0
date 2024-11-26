package com.syi.project.attendance.controller;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AdminAttendListDTO;
import com.syi.project.attendance.dto.response.AttendanceTotalResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.service.AttendanceService;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.MemberService;
import com.syi.project.course.service.CourseService;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  // 출석 전체 조회_수강생
  @GetMapping("course/{courseId}/{attendanceId}")
  public ResponseEntity<Page<AdminAttendListDTO>> getAllAttendance(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @PathVariable Long attendanceId,
      @RequestBody @Valid AttendanceRequestDTO attendanceRequestDTO,
      Pageable pageable) {
    log.info("출석 전체 조회 요청");
    log.info("조회 요청된 attendanceRequestDTO: {}", attendanceRequestDTO);
    /*AttendanceResponseDTO.AdminAttendListDTO attendances = attendanceService.getAllAttendances(
        userDetails, courseId, attendanceRequestDTO, pageable);
    log.info("출석 조회된 정보: {}", attendances);
    return ResponseEntity.ok(attendances);*/
    return null;
  }

  //  출석 상세 조회_수강생
  @GetMapping("{id}")
  public ResponseEntity<AttendanceTotalResponseDTO> getAttendanceById(
      @Parameter(description = "상세 조회할 출석 ID", required = true) @PathVariable Long id,
      @RequestBody @Valid AttendanceRequestDTO attendanceRequestDTO) {
    log.info("출석 상세 조회 요청");
    log.info("상세 조회 요청된 attendanceRequestDTO: {}", attendanceRequestDTO);

    /* 출석 목록 조회 */
    List<AttendanceResponseDTO> attendanceInfo = attendanceService.findAttendanceById(
        attendanceRequestDTO);
    log.info("출석 상세 조회 완료 {}", attendanceInfo.size());

    /* 시간표 정보 조회 */
    ScheduleResponseDTO scheduleInfo = scheduleService.getScheduleById(
        attendanceRequestDTO.getCourseId());

    AttendanceTotalResponseDTO responseDTO = AttendanceTotalResponseDTO.builder()
        .attendanceInfo(attendanceInfo)
        .scheduleInfo(scheduleInfo)
        .build();
    return ResponseEntity.ok(responseDTO);
  }



}

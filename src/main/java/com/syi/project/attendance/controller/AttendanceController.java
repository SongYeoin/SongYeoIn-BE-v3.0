package com.syi.project.attendance.controller;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.request.AttendanceRequestDTO.StudentAllAttendRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendanceTableDTO;
import com.syi.project.attendance.service.AttendanceService;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.MemberService;
import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.service.CourseService;
import com.syi.project.period.dto.PeriodResponseDTO;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
  public AttendanceResponseDTO createAttendance(
      @AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request,
      @RequestBody AttendanceRequestDTO attendanceRequestDTO) {
    log.info("출석 등록 요청");
    AttendanceResponseDTO responseDTO = attendanceService.createAttendance(userDetails, attendanceRequestDTO.getCourseId(),
        attendanceRequestDTO.isEntering(), request);
    log.info("출석 등록 완료");

    return responseDTO;
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
  @GetMapping("course/{courseId}/detail")
  public ResponseEntity<AttendDetailDTO> getAttendanceById(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
  Pageable pageable) {
    log.info("출석 상세 조회 요청");

   /* 출석 목록 조회 */
    AttendDetailDTO attendance = attendanceService.findAttendanceByIds(
       userDetails, courseId,
        userDetails.getId(), date,pageable);

    log.info("조회된 학생 출석 정보: {}", attendance);
    return ResponseEntity.ok(attendance);
  }

  @GetMapping("/courses")
  public ResponseEntity<List<CourseListDTO>> getAllCoursesByAdminId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("adminId: {} 가 맡고 있는 교육과정 조회 요청", userDetails.getId());
    List<CourseDTO.CourseListDTO> courses = attendanceService.getAllCoursesByStudentId(userDetails);
    log.debug("총 {} 개의 교육과정 조회 완료", courses.size());
    return ResponseEntity.ok(courses);
  }

  @GetMapping("/main/{courseId}")
  public ResponseEntity<List<AttendanceTableDTO>> getAttendanceByDate(
      @PathVariable Long courseId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<AttendanceTableDTO> attendanceList = attendanceService.getAttendanceByCourseAndDate(
        userDetails, courseId, date);
    return ResponseEntity.ok(attendanceList);
  }

  /* 메인 페이지에서 초기 로딩으로 해당 반의 시간표 불러오기 */
  @GetMapping("/period/all/{courseId}")
  public ResponseEntity<List<PeriodResponseDTO>> getPeriodsByDateAndDayOfWeek(
      @PathVariable Long courseId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<PeriodResponseDTO> attendanceList = attendanceService.getPeriodsByDateAndDayOfWeek(
        userDetails, courseId);
    return ResponseEntity.ok(attendanceList);
  }

  @GetMapping("/course/{courseId}/rate")
  public ResponseEntity<Map<String, Object>> getRateByCourseId(@PathVariable Long courseId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    log.debug("출석률 조회할 memberId: {}", memberId);

    Map<String, Object> rateMap = attendanceService.getStudentAttendanceRates(memberId,courseId);
    log.debug("rateMap: {}", rateMap);

    return ResponseEntity.ok(rateMap);
  }


}

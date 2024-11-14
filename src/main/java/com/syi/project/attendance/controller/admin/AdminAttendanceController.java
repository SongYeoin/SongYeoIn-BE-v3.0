package com.syi.project.attendance.controller.admin;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.service.AttendanceService;
import com.syi.project.auth.service.MemberService;
import com.syi.project.course.service.CourseService;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/attendance")
@Slf4j
@RequiredArgsConstructor // final 이 붙은 필드 생성자 자동 주입
@Tag(name = "attendance", description = "출석 API")
public class AdminAttendanceController {

  private final AttendanceService attendanceService;
  private final CourseService courseService;
  private final ScheduleService scheduleService;
  //private final EnrollService enrollService;
  private final MemberService memberService;

  // 출석 전체 조회
  @GetMapping
  @Operation(summary = "출석 전체 조회", description = "출석을 전체 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "출석을 성공적으로 조회했습니다."),
      })
  public ResponseEntity<AttendanceResponseDTO> getAllAttendances(
      @RequestBody AttendanceRequestDTO attendanceRequestDTO) {
    log.info("출석 전체 조회 요청");
    log.info("attendanceRequestDTO: {}", attendanceRequestDTO);
    AttendanceResponseDTO attendances = attendanceService.getAllAttendances(attendanceRequestDTO);
    log.info("조회된 출석 정보: {}", attendances);
    return ResponseEntity.ok(attendances);
  }

  //  수강생 별 출석 조회
  @GetMapping("/detail/{id}")
  public ResponseEntity<AttendanceResponseDTO> getAttendanceDetail(
      @Parameter(description = "상세 조회할 수강생 ID", required = true) @PathVariable Long memberId) {

  }


}

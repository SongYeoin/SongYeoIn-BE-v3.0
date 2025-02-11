package com.syi.project.attendance.controller.admin;

import com.syi.project.attendance.dto.request.AttendanceRequestDTO.AllAttendancesRequestDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendDetailDTO;
import com.syi.project.attendance.dto.response.AttendanceResponseDTO.AttendListResponseDTO;
import com.syi.project.attendance.service.AttendanceService;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.MemberService;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.service.CourseService;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/attendance")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "manager-attendance", description = "담당자 출석 API")
public class AdminAttendanceController {

  private final AttendanceService attendanceService;
  private final CourseService courseService;
  private final ScheduleService scheduleService;
  //private final EnrollService enrollService;
  private final MemberService memberService;

  // 출석 전체 조회(반별 조회 가능)
  @Operation(summary = "출석 전체 조회", description = "출석을 전체 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "출석을 성공적으로 조회했습니다."),
      })
  @GetMapping("/course/{courseId}") //기본적으로 날짜도 같이 옴
  public ResponseEntity<Page<AttendListResponseDTO>> getAllAttendances(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
      @RequestParam(required = false) String studentName,
      @RequestParam(required = false) String status,
      @PageableDefault(size = 10) Pageable pageable) {

    log.info("관리자 출석 전체 조회 요청");
    log.debug("출석 조회 요청 자격: {}, PK: {}", userDetails.getAuthorities(), userDetails.getId());
    log.info("date: {}, studentName: {}, status: {}", date, studentName, status);

    AllAttendancesRequestDTO requestDTO = AllAttendancesRequestDTO.builder()
        .date(date)
        .studentName(studentName)
        .status(status)
        .build();

    Page<AttendListResponseDTO> attendances = attendanceService.getAllAttendancesForAdmin(
        userDetails, courseId,
        requestDTO, pageable);

    log.info("조회된 출석 정보: {} ", attendances.getContent());
    return ResponseEntity.ok(attendances);
  }


  //  수강생 별 출석 조회(상세보기)
  @Operation(summary = "수강생 출석 상세 조회", description = "수강생 출석을 상세 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수강생 출석이 성공적으로 조회되었습니다."),
      })
  @GetMapping("/course/{courseId}/member/{studentId}")    //기본적으로 날짜도 같이 옴
  public ResponseEntity<AttendDetailDTO> getAttendanceByMemberId(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long courseId,
      @PathVariable Long studentId,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
      Pageable pageable) {
    log.info("수강생 ID {} 출석 조회 요청", studentId);
    log.debug("studentId: {}, courseId: {}", studentId, courseId);
    AttendDetailDTO memberAttendance = attendanceService.findAttendanceByIds(
        userDetails, courseId, studentId, date, pageable);
    log.info("조회된 학생 출석 정보: {}", memberAttendance);
    return ResponseEntity.ok(memberAttendance);
  }

  //  상세 출석 수정하기
  @PatchMapping("{id}")
  @Operation(summary = "상세 출석 수정", description = "상세 출석을 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "상세 출석을 성공적으로 수정했습니다."),
      })
  public ResponseEntity<AttendanceResponseDTO> updateAttendanceById(@PathVariable Long id,
      @RequestParam String status) {
    log.info("출석 ID {} 출석 수정 요청", id);
    log.info("수정 요청 출석상태: {}", status);
    AttendanceResponseDTO updateStatus = attendanceService.updateAttendance(id, status);
    log.info("수정 완료");
    return ResponseEntity.ok(updateStatus);
  }

/*//  일괄 결석 처리
  @PostMapping("/absent")
  @Operation(summary = "일괄 결석 처리", description = "선택된 학생들을 일괄 결석 처리합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "일괄 결석 처리를 성공적으로 수정했습니다."),
      })
  public ResponseEntity<AttendanceResponseDTO> updateAbsentStatus(@RequestBody @Valid AttendanceRequestDTO attendanceRequestDTO){
    log.info("일괄 결석 처리 요청");
    log.info("일괄 결석 처리 요청된 출석 id: {}", attendanceRequestDTO.getId());
    //AttendanceResponseDTO updateAbsent =
    attendanceService.updateAbsentStatus(attendanceRequestDTO);
    //log.info("일괄 결석 처리 완료된 정보: {}", updateAbsent);
    return ResponseEntity.noContent().build();
  }*/

  @GetMapping("/courses")
  public ResponseEntity<List<CourseListDTO>> getAllCoursesByAdminId(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("adminId: {} 가 맡고 있는 교육과정 조회 요청", userDetails.getId());
    List<CourseListDTO> courses = attendanceService.getAllCoursesByAdminId(userDetails);
    log.debug("총 {} 개의 교육과정 조회 완료", courses.size());
    return ResponseEntity.ok(courses);
  }

  @GetMapping("/course/{courseId}/rate")
  public ResponseEntity<Map<Long, Map<String, Object>>> getAllStudentRatesByCourseId(@PathVariable Long courseId) {

    log.info("관리자 출석률 조회 요청 - Course ID: {}", courseId);

    Map<Long, Map<String, Object>> ratesMap = attendanceService.getAllStudentsAttendanceRates(courseId);
    log.debug("ratesMap: {}", ratesMap);

    return ResponseEntity.ok(ratesMap);
  }


}

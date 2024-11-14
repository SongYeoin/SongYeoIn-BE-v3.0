package com.syi.project.enroll.controller;

import com.syi.project.enroll.dto.EnrollRequestDTO;
import com.syi.project.enroll.dto.EnrollResponseDTO;
import com.syi.project.enroll.service.EnrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollController {

  private final EnrollService enrollService;

  @Operation(summary = "수강 신청", description = "courseId와 memberId를 통해 수강 신청을 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수강 신청 성공"),
      @ApiResponse(responseCode = "400", description = "요청 데이터 오류"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public ResponseEntity<EnrollResponseDTO> enrollCourse(@RequestBody EnrollRequestDTO requestDTO) {
    return ResponseEntity.ok(enrollService.enrollCourse(requestDTO));
  }

  @Operation(summary = "수강 신청 삭제", description = "수강 신청 ID와 회원 ID를 이용하여 수강 신청을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수강 신청 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "수강 신청을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("/{enrollmentId}")
  public ResponseEntity<Void> deleteEnrollment(@PathVariable Long enrollmentId,
      @RequestParam Long memberId) {
    enrollService.deleteEnrollment(enrollmentId, memberId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "내 수강 이력 조회", description = "특정 회원의 활성화된 수강 이력을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수강 이력 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<List<EnrollResponseDTO>> getMyEnrollments(@RequestParam Long memberId) {
    return ResponseEntity.ok(enrollService.findEnrollmentsByMemberId(memberId));
  }

}

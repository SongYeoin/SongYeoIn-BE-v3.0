package com.syi.project.enroll.controller;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/enrollments")
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

  @Operation(summary = "특정 회원의 수강 이력 조회", description = "특정 회원의 수강 이력을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수강 이력 조회 성공"),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<List<EnrollResponseDTO>> getEnrollmentsByMemberId(@RequestParam Long memberId) {
    log.info("특정 회원의 수강 이력 조회 요청 - memberId: {}", memberId);
    return ResponseEntity.ok(enrollService.findEnrollmentsByMemberId(memberId));
  }

  @Operation(summary = "내 수강 이력 조회", description = "로그인한 사용자의 수강 이력을 조회합니다. 관리자는 모든 교육과정을, 학생은 본인의 수강이력만 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수강 이력 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/my")
  public ResponseEntity<List<EnrollResponseDTO>> getMyEnrollments(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
    }
    Long memberId = userDetails.getId();
    Role role = userDetails.getRole();
    log.info("내 수강 이력 조회 요청 - memberId: {}, role: {}", memberId, role);
    return ResponseEntity.ok(enrollService.findMyEnrollments(memberId, role));
  }

}

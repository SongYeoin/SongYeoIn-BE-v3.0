package com.syi.project.schedule.controller;


import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.schedule.dto.ScheduleRequestDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/schedule")
@Slf4j
@RequiredArgsConstructor // final 이 붙은 필드 생성자 자동 주입
@Tag(name = "schedule", description = "시간표 API")
public class ScheduleController {

  private final ScheduleService scheduleService;

  /* 시간표 등록 */
  @Operation(summary = "시간표 등록", description = "시간표를 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "201", description = "시간표가 성공적으로 등록되었습니다."),
      })
  @PostMapping
  public ResponseEntity<ScheduleResponseDTO> createSchedule(
      @Parameter(description = "시간표 등록 정보", required = true) @Valid @RequestBody ScheduleRequestDTO scheduleDTO) {
    log.info("Create Schedule with data : {}", scheduleDTO);
    ScheduleResponseDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
  }


  /* 시간표 상세 조회 */
  @Operation(summary = "시간표 상세 조회", description = "등록된 시간표를 상세 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "시간표가 성공적으로 조회되었습니다."),
      })
  @GetMapping("{id}")
  public ResponseEntity<ScheduleResponseDTO> getScheduleById(
      @Parameter(description = "상세 조회할 시간표 ID", required = true) @PathVariable Long id) {
    log.info("Request to get schedule with ID: {}", id);
    ScheduleResponseDTO schedule = scheduleService.getScheduleById(id);
    log.info("get schedule with ID: {} successfully", id);
    return ResponseEntity.ok(schedule);
  }

 /* @PatchMapping("{id}")
  @Operation(summary = "시간표 수정", description = "시간표를 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "시간표를 성공적으로 수정했습니다."),
      })
  public ResponseEntity<ScheduleResponseDTO> updateSchedule(
      @Parameter(description = "수정할 시간표를의 ID", required = true) @PathVariable Long id,
      @RequestBody ScheduleRequestDTO scheduleRequestDTO) {
    log.info("Request to update schedule with ID: {}. Update data: {}", id, scheduleRequestDTO);
    ScheduleResponseDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleRequestDTO);
    log.info("Schedule with ID: {} updated successfully", id);
    return ResponseEntity.ok(updatedSchedule);
  }
*/


  // 반 ID 얻어와서 해당하는 시간표 다 삭제
  @DeleteMapping("{id}")
  @Operation(summary = "시간표 삭제", description = "시간표를 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "시간표가 성공적으로 삭제되었습니다."),
      })
  public ResponseEntity<Void> deleteSchedule(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "삭제할 시간표의 ID", required = true) @PathVariable Long id) {
    log.info("Request to delete schedule with ID: {}", id);
    scheduleService.deletePeriod(userDetails.getId(),id);
    log.info("Schedule with ID: {} deleted successfully", id);
    return ResponseEntity.noContent().build();
  }

}

package com.syi.project.attendance.dto.response;

import com.syi.project.schedule.dto.ScheduleResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "출석 상세보기 응답 DTO")
public class AttendanceTotalResponseDTO {

/*  @Schema(description = "수강생 정보")
  private UserInfoDTO userInfo;*/

  @Schema(description = "시간표 정보")
  private ScheduleResponseDTO scheduleInfo;

  @Schema(description = "출석 상태 목록")
  private List<AttendanceResponseDTO> attendanceInfo;

  @Builder
  public AttendanceTotalResponseDTO(ScheduleResponseDTO scheduleInfo,
      List<AttendanceResponseDTO> attendanceInfo) {
    this.scheduleInfo = scheduleInfo;
    this.attendanceInfo = attendanceInfo;
  }

  public static AttendanceTotalResponseDTO fromEntity(ScheduleResponseDTO scheduleInfo,
      List<AttendanceResponseDTO> attendanceInfo) {

    return AttendanceTotalResponseDTO.builder()
        .scheduleInfo(scheduleInfo)
        .attendanceInfo(attendanceInfo)
        .build();
  }
}

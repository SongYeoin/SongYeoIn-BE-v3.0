package com.syi.project.attendance.dto.response;

import com.syi.project.period.dto.PeriodResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Getter
@ToString
@Schema(description = "출석 응답 DTO")
public class AttendanceResponseDTO {

  @Getter
  @ToString
  public static class AttendListResponseDTO {

    private Long studentId;
    private String studentName;
    private String courseName;
    private LocalDate date;
    //private List<AttendanceDTO> attendanceList;
    // 교시별 출석 상태 (교시 ID를 키로 사용)
    private List<String> periods;
    private Map<String, String> students; //교시명, 출석상태

    @Builder
    public AttendListResponseDTO(Long studentId, String studentName, String courseName,
        LocalDate date, List<String> periods,Map<String, String> students) {
      this.studentId = studentId;
      this.studentName = studentName;
      this.courseName = courseName;
      this.date = date;
      this.periods = periods;
      this.students = students;
    }
  }
  @Getter
  @ToString
  public static class StudentAttendListResponseDTO {

    private LocalDate date;
    // 교시별 출석 상태
    private Map<String, String> students;
    private List<String> periods;

    @Builder
    public StudentAttendListResponseDTO(
        LocalDate date, Map<String, String> students, List<String> periods) {
      this.date = date;
      this.students = students;
      this.periods = periods;
    }
  }

  @Getter
  @ToString
  public static class MemberInfoInDetail {

    private String studentName;
    private String courseName;
    private LocalDate date;
    private String adminName;

    @Builder
    public MemberInfoInDetail(String studentName, String courseName, LocalDate date,
        String adminName) {
      this.studentName = studentName;
      this.courseName = courseName;
      this.date = date;
      this.adminName = adminName;
    }
  }


  @Getter
  @ToString
  public static class AttendDetailDTO {

    private MemberInfoInDetail memberInfo;
    private List<PeriodResponseDTO> periodList;
    private Page<AttendanceStatusListDTO> attendances;

    @Builder
    public AttendDetailDTO(MemberInfoInDetail memberInfo,
        List<PeriodResponseDTO> periodList, Page<AttendanceStatusListDTO> attendances) {
      this.memberInfo = memberInfo;
      this.periodList = periodList;
      this.attendances = attendances;
    }
  }

  @Getter
  @ToString
  public static class AttendanceStatusListDTO {

    private Long attendanceId;
    private String periodName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime enrollDate;
    private String status;

    @Builder
    public AttendanceStatusListDTO(Long attendanceId, String periodName, LocalTime startTime, LocalTime endTime,
        LocalDateTime enrollDate, String status) {
      this.attendanceId = attendanceId;
      this.periodName = periodName;
      this.startTime = startTime;
      this.endTime = endTime;
      this.enrollDate = enrollDate;
      this.status = status;
    }
  }


  @Getter
  @ToString
  public static class AttendanceTableDTO{
    private String periodName;
    private String status; // 출석, 결석, 지각 등
    private Long periodId;
    private LocalDateTime enterTime;
    private LocalDateTime exitTime;

    @Builder
    public AttendanceTableDTO(String periodName, String status, Long periodId, LocalDateTime enterTime,LocalDateTime exitTime ) {
      this.periodName = periodName;
      this.status = status;
      this.periodId = periodId;
      this.enterTime = enterTime;
      this.exitTime = exitTime;
    }
  }


  private Long attendanceId;

  @Schema(description = "출석 상태", example = "")
  @NotNull(message = "출석 상태는 필수입니다.")
  private String status;

  @Schema(description = "출석 날짜", example = "")
  private LocalDate date;

  @Schema(description = "출석 등록일", example = "")
  @PastOrPresent(message = "등록일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDateTime enrollDate;

  @Schema(description = "출석 수정일", example = "")
  @PastOrPresent(message = "수정일은 과거 또는 현재 날짜여야 합니다.")
  private LocalDateTime modifiedDate;

  @Schema(description = "교시 ID", example = "1")
  @NotNull(message = "교시 ID는 필수입니다.")
  private Long periodId;

  @Schema(description = "교육과정 ID", example = "1")
  @NotNull(message = "교육과정 ID는 필수입니다.")
  private Long courseId;

  @Schema(description = "수강생 ID", example = "1")
  @NotNull(message = "수강생 ID는 필수입니다.")
  private Long memberId;

  @Size(max = 255, message = "메모는 최대 255자까지 가능합니다.")
  private String memo;

  @Schema(description = "예외시 표시 메시지", example = "학원 네트워크에서만 출석이 가능합니다.")
  private String resultMessage;

  @Schema(description = "입실 시간", example = "03:11:12 2025-01-22")
  private LocalDateTime enterTime; // 입실 시간

  @Schema(description = "퇴실 시간", example = "03:11:12 2025-01-22")
  private LocalDateTime exitTime; // 퇴실 시간


  @Builder
  public AttendanceResponseDTO(Long attendanceId, String status, LocalDate date,
      LocalDateTime enrollDate, LocalDateTime modifiedDate, Long periodId, Long courseId,
      Long memberId, String memo ,String resultMessage, LocalDateTime enterTime, LocalDateTime exitTime) {
    this.attendanceId = attendanceId;
    this.status = status;
    this.date = date;
    this.enrollDate = enrollDate;
    this.modifiedDate = modifiedDate;
    this.periodId = periodId;
    this.courseId = courseId;
    this.memberId = memberId;
    this.memo = memo;
    this.resultMessage = resultMessage;
    this.enterTime = enterTime;
    this.exitTime = exitTime;
  }

}

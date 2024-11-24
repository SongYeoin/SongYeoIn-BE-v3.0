package com.syi.project.course.dto;

import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.period.dto.PeriodResponseDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Page;


public class CourseResponseDTO {

  @Getter
  @ToString
  @NoArgsConstructor
  public static class AdminList {

    private Long id; // 담당자 id
    private String name;  // 담당자 명

    public AdminList(Long id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  @Getter
  @ToString
  @NoArgsConstructor
  public static class CourseDetailDTO {

    private CourseDTO course;
    private ScheduleResponseDTO schedule;

    @Builder
    public CourseDetailDTO(CourseDTO course, ScheduleResponseDTO schedule) {
      this.course = course;
      this.schedule = schedule;
    }
  }
}

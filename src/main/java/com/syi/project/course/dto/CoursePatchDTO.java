package com.syi.project.course.dto;

import com.syi.project.schedule.dto.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO.ScheduleUpdateResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "교육 과정 수정 DTO")
public class CoursePatchDTO {

  private CourseDTO course;
  private ScheduleUpdateRequestDTO schedule;

  @Builder
  public CoursePatchDTO(CourseDTO course, ScheduleUpdateRequestDTO schedule) {
    this.course = course;
    this.schedule = schedule;
  }


  @Getter
  @ToString
  public static class CoursePatchResponseDTO {

    private CourseDTO course;
    private ScheduleUpdateResponseDTO schedule;

    @Builder
    public CoursePatchResponseDTO(CourseDTO course, ScheduleUpdateResponseDTO schedule) {
      this.course = course;
      this.schedule = schedule;
    }
  }

}

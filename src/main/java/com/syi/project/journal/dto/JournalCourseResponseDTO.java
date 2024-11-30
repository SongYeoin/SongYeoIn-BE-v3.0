package com.syi.project.journal.dto;

import com.syi.project.course.entity.Course;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalCourseResponseDTO {
  private Long id;
  private String name;
  private LocalDate startDate;
  private LocalDate endDate;

  @Builder
  public JournalCourseResponseDTO(Long id, String name, LocalDate startDate, LocalDate endDate) {
    this.id = id;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static JournalCourseResponseDTO of(Course course) {
    return JournalCourseResponseDTO.builder()
        .id(course.getId())
        .name(course.getName())
        .startDate(course.getStartDate())
        .endDate(course.getEndDate())
        .build();
  }
}
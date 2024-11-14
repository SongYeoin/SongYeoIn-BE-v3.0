package com.syi.project.enroll.dto;

import com.syi.project.auth.entity.Member;
import com.syi.project.course.entity.Course;
import com.syi.project.enroll.entity.Enroll;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnrollRequestDTO {

  private Long memberId;
  private Long courseId;

  public Enroll toEntity() {
    return new Enroll(courseId, memberId);
  }

}

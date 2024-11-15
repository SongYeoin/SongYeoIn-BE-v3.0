package com.syi.project.enroll.dto;

import com.syi.project.enroll.entity.Enroll;
import lombok.Getter;

@Getter
public class EnrollResponseDTO {

  private Long id;
  private Long courseId;
  private Long memberId;
  private Long deletedBy;

  public EnrollResponseDTO(Enroll enroll) {
    this.id = enroll.getId();
    this.courseId = enroll.getCourseId();
    this.memberId = enroll.getMemberId();
    this.deletedBy = enroll.getDeletedBy();
  }

}

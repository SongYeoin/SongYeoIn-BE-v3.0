package com.syi.project.enroll.repository;

import com.syi.project.auth.entity.Member;
import com.syi.project.enroll.entity.Enroll;
import java.util.List;

public interface EnrollRepositoryCustom {

  List<Enroll> findEnrollmentsByMemberId(Long memberId);

  void deleteEnrollment(Long enrollmentId, Long memberId);

  List<Member> findStudentByCourseId(Long courseId);

}

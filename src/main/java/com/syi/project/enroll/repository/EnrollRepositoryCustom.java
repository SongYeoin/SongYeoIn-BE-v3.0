package com.syi.project.enroll.repository;

import com.syi.project.auth.entity.Member;
import com.syi.project.enroll.entity.Enroll;
import java.util.List;
import java.util.Map;

public interface EnrollRepositoryCustom {

  List<Enroll> findEnrollmentsByMemberId(Long memberId);

  void deleteEnrollment(Long enrollmentId, Long memberId);

  List<Member> findStudentByCourseId(Long courseId);

  Map<Long, Integer> countEnrollsByCourseIds(List<Long> courseIds);
}

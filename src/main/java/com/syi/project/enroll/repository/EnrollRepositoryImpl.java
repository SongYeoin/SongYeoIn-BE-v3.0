package com.syi.project.enroll.repository;

import static com.syi.project.enroll.entity.QEnroll.enroll;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.auth.entity.Member;
import com.syi.project.enroll.entity.Enroll;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EnrollRepositoryImpl implements EnrollRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Enroll> findEnrollmentsByMemberId(Long memberId) {
    return queryFactory
        .selectFrom(enroll)
        .where(enroll.memberId.eq(memberId).and(enroll.deletedBy.isNull()))
        .fetch();
  }

  @Override
  public void deleteEnrollment(Long enrollmentId, Long memberId) {
    queryFactory.update(enroll)
        .set(enroll.deletedBy, memberId)
        .where(enroll.id.eq(enrollmentId))
        .execute();
  }

  @Override
  public List<Member> findStudentByCourseId(Long courseId) {
    return List.of();
  }

  @Override
  public Map<Long, Integer> countEnrollsByCourseIds(List<Long> courseIds) {
    return queryFactory
        .select(enroll.courseId, enroll.count())
        .from(enroll)
        .where(enroll.courseId.in(courseIds))
        .groupBy(enroll.courseId)
        .fetch()
        .stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(enroll.courseId),
            tuple -> Objects.requireNonNull(tuple.get(enroll.count())).intValue()
        ));
  }

  @Override
  public List<Enroll> findEnrollmentsByCourseId(Long courseId) {
    return queryFactory.selectFrom(enroll)
        .where(enroll.courseId.eq(courseId))
        .fetch();
  }

  @Override
  public void deleteEnrollmentByCourseId(Long adminId, Long courseId) {
    queryFactory.update(enroll)
        .set(enroll.deletedBy, adminId)
        .where(enroll.courseId.eq(courseId))
        .execute();
  }
}

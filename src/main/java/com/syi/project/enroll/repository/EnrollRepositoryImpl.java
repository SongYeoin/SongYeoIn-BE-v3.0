package com.syi.project.enroll.repository;

import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.enroll.entity.QEnroll.enroll;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.auth.entity.Member;
import com.syi.project.enroll.entity.Enroll;
import java.util.List;
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

  public List<Member> findStudentByCourseId(Long courseId) {
    return queryFactory.select(member)
        .from(enroll)
        .join(member).on(enroll.memberId.eq(member.id))
        .where(enroll.courseId.eq(courseId))
        .fetch();
  }
}

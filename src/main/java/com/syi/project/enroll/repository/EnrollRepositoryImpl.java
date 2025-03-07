package com.syi.project.enroll.repository;

import static com.syi.project.auth.entity.QMember.member;
import static com.syi.project.enroll.entity.QEnroll.enroll;
import static com.syi.project.course.entity.QCourse.course;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.auth.entity.Member;
import com.syi.project.enroll.dto.EnrollResponseDTO;
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
  public List<EnrollResponseDTO> findEnrollmentsByMemberId(Long memberId) {
    return queryFactory
        .select(Projections.constructor(
            EnrollResponseDTO.class,
            enroll.id,
            course.id,
            course.name,
            course.adminName,
            course.teacherName,
            course.enrollDate,
            course.startDate,
            course.endDate
        ))
        .from(enroll)
        .join(course).on(enroll.courseId.eq(course.id))
        .where(enroll.memberId.eq(memberId)
            .and(enroll.deletedBy.isNull())
            .and(course.deletedBy.isNull())
        )
        .fetch();
  }

  @Override
  public List<EnrollResponseDTO> findAllActiveCourses() {
    return queryFactory
        .select(Projections.constructor(
            EnrollResponseDTO.class,
            course.id,
            course.id,
            course.name,
            course.adminName,
            course.teacherName,
            course.enrollDate,
            course.startDate,
            course.endDate
        ))
        .from(course)
        .where(course.deletedBy.isNull())
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
    return queryFactory.select(member)
        .from(enroll)
        .join(member).on(enroll.memberId.eq(member.id))
        .where(enroll.courseId.eq(courseId))
        .fetch();
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
  public void deleteEnrollmentByCourseId(Long enrollId,Long adminId, Long courseId) {
    queryFactory.update(enroll)
        .set(enroll.deletedBy, adminId)
        .where(enroll.courseId.eq(courseId).and(enroll.id.eq(enrollId)))
        .execute();
  }

  @Override
  public List<Long> findStudentIdByCourseId(Long courseId) {
    return queryFactory.select(enroll.memberId)
        .from(enroll)
        .where(enroll.courseId.eq(courseId)
            .and(enroll.deletedBy.isNull()))
        .fetch();
  }

  @Override
  public boolean existsByMemberIdAndCourseId(Long memberId, Long courseId) {
    Integer fetchOne = queryFactory
        .selectOne()
        .from(enroll)
        .where(
            enroll.memberId.eq(memberId)
                .and(enroll.courseId.eq(courseId))
                .and(enroll.deletedBy.isNull())
        )
        .fetchFirst();  // limit 1
    return fetchOne != null;
  }
}

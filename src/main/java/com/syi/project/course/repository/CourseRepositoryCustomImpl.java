package com.syi.project.course.repository;

import static com.syi.project.course.entity.QCourse.course;
import static com.syi.project.enroll.entity.QEnroll.enroll;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.entity.Course;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CourseRepositoryCustomImpl implements
    CourseRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public CourseRepositoryCustomImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public Page<Course> findCoursesById(Long adminId, String type, String word, Pageable pageable) {
    log.debug("findCoursesById : adminId={}", adminId);

    BooleanBuilder predicate = new BooleanBuilder(course.deletedBy.isNull());

    // 필터링 조건이 하나밖에 안된다는 전체
    /*if (type != null && !TextUtils.isBlank(word)) {
      switch (type) {
        case "name" -> predicate.and(course.name.containsIgnoreCase(word));
        case "roomName" -> predicate.and(course.roomName.containsIgnoreCase(word));
        //case "status" -> predicate.and(course.status.in(word);
      }
    }*/
    if(!TextUtils.isBlank(word)) {
      predicate.and(course.name.containsIgnoreCase(word));
    }

    // 현재 페이지 데이터
    List<Course> content = queryFactory.selectFrom(course)
        .where(predicate.and(course.deletedBy.isNull()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(course.enrollDate.desc(),course.name.asc())
        .fetch();

    // 데이터가 없어도 예외발생 하지 않도록 처리
    if (content.isEmpty()) {
      log.debug("조건에 맞는 데이터가 없습니다.");
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    // 전체 데이터 개수
    Long count = queryFactory.select(course.count())
        .from(course)
        .where(predicate.and(course.deletedBy.isNull()))
        .fetchOne();

    return new PageImpl<>(content, pageable, count);
  }

  @Override
  public List<CourseListDTO> findCoursesByAdminId(Long adminId) {
    return queryFactory
        .select(Projections.constructor(
            CourseListDTO.class,
            course.id.as("courseId"),     // DTO 필드 이름과 매핑
            course.name.as("courseName") // DTO 필드 이름과 매핑
        ))
        .from(course)
        .where(course.adminId.eq(adminId)
            .and(course.deletedBy.isNull()))
        .orderBy(course.id.asc())
        .fetch();
  }

  @Override
  public List<CourseListDTO> findCoursesByStudentId(Long studentId) {
    return queryFactory.select(Projections.constructor(
            CourseListDTO.class,
            course.id.as("courseId"),     // DTO 필드 이름과 매핑
            course.name.as("courseName") // DTO 필드 이름과 매핑
        ))
        .from(course)
        .join(enroll).on(course.id.eq(enroll.courseId))
        .where(enroll.memberId.eq(studentId)
            .and(enroll.deletedBy.isNull())
            .and(course.deletedBy.isNull()))
        .fetch();
  }
}

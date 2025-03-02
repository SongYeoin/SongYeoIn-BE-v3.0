package com.syi.project.course.repository;

import com.syi.project.course.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

  List<Course> findByDeletedByIsNull();

  Course findCourseById(Long courseId);

  /**
   * 활성 상태인 모든 수업을 조회합니다.
   */
  @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE'")
  List<Course> findAllActiveCourses();
}

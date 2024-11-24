package com.syi.project.course.repository;

import com.syi.project.course.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

  List<Course> findByDeletedByIsNull();

}

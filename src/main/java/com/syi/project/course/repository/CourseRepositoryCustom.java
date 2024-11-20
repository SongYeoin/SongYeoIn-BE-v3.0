package com.syi.project.course.repository;

import com.syi.project.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRepositoryCustom {

  Page<Course> findCoursesById(Long memberId, String type, String word, Pageable pageable);
}

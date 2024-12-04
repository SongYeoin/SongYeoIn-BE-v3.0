package com.syi.project.course.repository;

import com.querydsl.core.Tuple;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.entity.Course;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRepositoryCustom {

  Page<Course> findCoursesById(Long memberId, String type, String word, Pageable pageable);

  List<CourseListDTO> findCoursesByAdminId(Long adminId);

  List<CourseListDTO> findCoursesByStudentId(Long id);
}

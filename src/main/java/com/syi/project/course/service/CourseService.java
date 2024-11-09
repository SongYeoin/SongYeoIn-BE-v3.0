package com.syi.project.course.service;

import com.syi.project.course.dto.CourseDTO;
import java.util.List;
import java.util.Optional;

public interface CourseService {

  List<CourseDTO> getAllCourses();

  CourseDTO getCourseById(long id);

  CourseDTO createCourse(CourseDTO courseDTO);

  CourseDTO updateCourse(Long id,CourseDTO courseDTO);

  void deleteCourse(Long id);


}

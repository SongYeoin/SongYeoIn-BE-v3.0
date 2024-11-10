package com.syi.project.course.service;

import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import java.util.List;

public interface CourseService {

  List<CourseDTO> getAllCourses();

  CourseDTO getCourseById(long id);

  CourseDTO createCourse(CourseDTO courseDTO);

  CourseDTO updateCourse(Long id, CoursePatchDTO coursePatchDTO);

  void deleteCourse(Long id);


}

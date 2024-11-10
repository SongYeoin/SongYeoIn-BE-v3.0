package com.syi.project.course.controller;

import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import com.syi.project.course.service.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/courses")
@Slf4j
@RequiredArgsConstructor // final 이 붙은 필드 생성자 자동 주입
@Tag(name = "course", description = "교육과정 API")
public class CourseController {

  private final CourseService courseService;

  /* 교육과정 등록 */
  @PostMapping
  public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
    log.info("교육과정 등록 함수(Controller)....");
    log.info(courseDTO.toString());
    CourseDTO createdCourse = courseService.createCourse(courseDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
  }

  /* 교육과정 전체 조회 */
  @GetMapping
  public ResponseEntity<List<CourseDTO>> getAllCourses() {
    log.info("교육과정 전체 조회 함수(Controller)....");
    List<CourseDTO> courses = courseService.getAllCourses();
    return ResponseEntity.ok(courses);
  }

  /* 교육과정 조회 */
  @GetMapping("{id}")
  public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
    log.info("교육과정 상세조회 함수(Controller)....");
    CourseDTO course = courseService.getCourseById(id);
    return ResponseEntity.ok(course);
  }

  @PatchMapping("{id}")
  public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id,
      @RequestBody CoursePatchDTO coursePatchDTO) {
    log.info("교육과정 수정 함수(Controller)....");
    log.info("수정할 데이터 => {}", coursePatchDTO.toString());
    CourseDTO updatedCourse = courseService.updateCourse(id, coursePatchDTO);
    return ResponseEntity.ok(updatedCourse);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
    log.info("교육과정 삭제 함수(Controller)....");
    log.info("교육과정 삭제 번호 (Controller)....{}", id);
    courseService.deleteCourse(id);
    return ResponseEntity.noContent().build();
  }
}

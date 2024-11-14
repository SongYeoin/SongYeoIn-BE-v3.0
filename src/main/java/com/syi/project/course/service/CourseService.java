package com.syi.project.course.service;

import static com.syi.project.course.dto.CourseDTO.fromEntity;

import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService{

  private final CourseRepository courseRepository;


  /* 교육과정 전체 조회 */
  public List<CourseDTO> getAllCourses() {
    log.info("get all courses");
    /*deletdBy가 null이 아니라면 조회하지 않기 추가하기*/
    List<Course> courses = courseRepository.findAll();

    if (courses.isEmpty()) {
      log.warn("No courses found in the database");
      throw new NoSuchElementException("No courses found");
    }

    log.info("get {} courses from the database", courses.size());
    // Course 엔티티 리스트를 DTO 리스트로 변환
    return courses.stream()
        .map(CourseDTO::fromEntity)
        .toList();
  }

  /* 교육과정 조회 */
  public CourseDTO getCourseById(long id) {
    /*deletdBy가 null이 아니라면 조회하지 않기 추가하기*/
    log.info("get course details for ID: {}", id);
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> {
          log.error("Course not found with ID: {}", id);
          return new NoSuchElementException("Course not found with id " + id);
        });
    log.info("get course details for ID: {}", id);
    return fromEntity(course);

  }

  /* 교육과정 등록 */
  @Transactional
  public CourseDTO createCourse(CourseDTO courseDTO) {
    log.info("Registering a new course with data : {}", courseDTO);

    /* 1. courseDTO를 Course 엔티티 형식으로 바꾸기 */
    Course course = courseDTO.toEntity();
    log.debug("Converted courseDTO to course: {}", course);

    Course savedCourse;
    /* 2. 저장하기(저장된 객체 반환) */
    try {
      log.debug("Attempting to save course entity in the repository");
      savedCourse = courseRepository.save(course);
      log.info("Course successfully created with ID: {}", savedCourse.getId());
    } catch (Exception e) {
      log.error("Error occurred while creating course: {}", e.getMessage(), e);
      throw e;
    }

    /* 3. 조회한 과정을 다시 dto 형식으로 바꿔서 return  */
    log.info("Course registered successfully with ID: {}", savedCourse.getId());
    return fromEntity(savedCourse);
  }


  /* 교육과정 수정 */
  @Transactional
  public CourseDTO updateCourse(Long id, CoursePatchDTO coursePatchDTO) {
    log.info("Updating course with ID: {}. Patch data: {}", id, coursePatchDTO);

    // 1. ID로 Course 조회, 없으면 예외 발생
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> {
          log.error("Course not found with ID: {}", id);
          return new NoSuchElementException("Course not found with id " + id);
        });

    log.debug("Original course data before update: {}", course);

    // 2. 필드 업데이트
    course.updateWith(coursePatchDTO);

    // 3. 업데이트된 Course를 저장
    Course updatedCourse = courseRepository.save(course);

    log.info("Course with ID: {} updated successfully", id);
    log.debug("Updated course data: {}", updatedCourse);

    // 4. 업데이트된 엔티티를 DTO로 변환하여 반환
    return fromEntity(updatedCourse);
  }

  /* 교육과정 삭제 */
  @Transactional
  public void deleteCourse(Long id) {
    log.info("Deleting course with ID: {}", id);

    Course existingCourse = courseRepository.findById(id)
        .orElseThrow(() -> {
          log.error("Course not found with ID: {}", id);
          return new NoSuchElementException("Course not found with id " + id);
        });

    // 2. 로그인한 사람의 id를 얻어오기
    //existingCourse.updateDeletedBy(memberId);

    // 3. 업데이트된 existingCourse 저장
    courseRepository.save(existingCourse);
    log.info("Course with ID: {} deleted successfully", id);


    /*courseRepository.deleteById(id);*/

  }
}

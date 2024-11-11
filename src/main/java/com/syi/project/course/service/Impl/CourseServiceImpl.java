package com.syi.project.course.service.Impl;

import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.course.service.CourseService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;


  /* 교육과정 전체 조회 */
  @Override
  @Transactional
  public List<CourseDTO> getAllCourses() {
    log.info("교육과정 전체 조회 함수(Service)....");
    List<Course> courses = courseRepository.findAll();

    if (courses.isEmpty()) {
      throw new RuntimeException("No courses found");
    }

    // Course 엔티티 리스트를 DTO 리스트로 변환
    return courses.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /* 교육과정 조회 */
  @Override
  @Transactional
  public CourseDTO getCourseById(long id) {
    log.info("교육과정 상세조회 함수(Service)....");
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
    return convertToDTO(course);

  }

  /* 교육과정 등록 */
  @Override
  @Transactional
  public CourseDTO createCourse(CourseDTO courseDTO) {
    log.info("교육과정 등록 함수(Service)....");
    log.info(courseDTO.toString());

    /* 1. courseDTO를 Course 엔티티 형식으로 바꾸기 */
    Course course = courseDTO.toEntity();


    /* 2. 저장하기(저장된 객체 반환) */
    Course savedCourse = courseRepository.save(course);

    /* 3. 조회한 과정을 다시 dto 형식으로 바꿔서 return  */
    return convertToDTO(savedCourse);
  }

  private CourseDTO convertToDTO(Course course) {

    return CourseDTO.builder()
        .id(course.getId())
            .name(course.getName())
                .description(course.getDescription())
                    .managerName(course.getManagerName())
                        .teacherName(course.getTeacherName())
                            .startDate(course.getStartDate())
                                .endDate(course.getEndDate())
                                    .roomName(course.getRoomName())
                                        .enrollDate(course.getEnrollDate())
                                            .modifiedDate(course.getModifiedDate())
                                                .managerNo(course.getManagerNo())
                                                    .build();
  }

  /* 교육과정 수정 */
  @Override
  @Transactional
  public CourseDTO updateCourse(Long id, CoursePatchDTO coursePatchDTO) {
    log.info("교육과정 수정 함수(Service)....");

    log.info("수정할 데이터 => {}", coursePatchDTO.toString());

    // 1. ID로 Course 조회, 없으면 예외 발생
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

    log.info("수정 전 course => {}", course.toString());

    // 2. 필드 업데이트
    course.updateWith(coursePatchDTO);

    // 3. 업데이트된 Course를 저장
    course = courseRepository.save(course);

    log.info("수정 후 course => {}", course.toString());

    // 4. 업데이트된 엔티티를 DTO로 변환하여 반환
    return convertToDTO(course);
  }

  /* 교육과정 삭제 */
  @Override
  public void deleteCourse(Long id) {

    log.info("교육과정 삭제 함수(Service)....");
    log.info("교육과정 삭제 번호(Service)....{}", id);

    Course existingCourse = courseRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

    // 2. isDeleted 필드를 true로 변경하기
    existingCourse.updateIsDeletedToTrue();

    // 3. 업데이트된 existingCourse 저장
    courseRepository.save(existingCourse);

    /*courseRepository.deleteById(id);*/

  }
}

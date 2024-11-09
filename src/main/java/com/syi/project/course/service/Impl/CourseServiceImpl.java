package com.syi.project.course.service.Impl;

import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.course.service.CourseService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

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
    /* courseDTO를 Course 엔티티 형식으로 바꾸기 */
    Course course = courseDTO.toEntity(courseDTO);


    /* 저장하기(저장된 객체 반환) */
    Course savedCourse = courseRepository.save(course);

    /* 조회한 과정을 다시 dto 형식으로 바꿔서 return  */
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
  public CourseDTO updateCourse(Long id,CourseDTO courseDTO) {
    return null;
  }

  /* 교육과정 삭제 */
  @Override
  public void deleteCourse(Long id) {

  }
}

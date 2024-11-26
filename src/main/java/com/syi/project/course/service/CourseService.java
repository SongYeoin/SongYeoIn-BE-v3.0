package com.syi.project.course.service;

import static com.syi.project.course.dto.CourseDTO.fromEntity;

import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CourseDTO.CourseListDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import com.syi.project.course.dto.CoursePatchDTO.CoursePatchResponseDTO;
import com.syi.project.course.dto.CourseResponseDTO.AdminList;
import com.syi.project.course.dto.CourseResponseDTO.CourseDetailDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.enroll.service.EnrollService;
import com.syi.project.period.repository.PeriodRepository;
import com.syi.project.schedule.dto.ScheduleResponseDTO;
import com.syi.project.schedule.dto.ScheduleResponseDTO.ScheduleUpdateResponseDTO;
import com.syi.project.schedule.repository.ScheduleRepository;
import com.syi.project.schedule.service.ScheduleService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService {

  private final CourseRepository courseRepository;
  private final MemberRepository memberRepository;
  private final EnrollRepository enrollRepository;
  private final ScheduleRepository scheduleRepository;
  private final PeriodRepository periodRepository;
  private final ScheduleService scheduleService;
  private final EnrollService enrollService;


  /* 교육과정 전체 조회 */
  public Page<CourseDTO> getAllCourses(Long adminId, String type, String word, Pageable pageable) {
    log.info("필터링된 교육 과정 조회 - 타입: {}, 검색어: {}", type, word);
    log.info("관리자 ID: {}", adminId);

    log.info("{} 관리자가 존재하는지 확인", adminId);
    Member member = memberRepository.findById(adminId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - adminId: {}", adminId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });
    log.info("존재하는 관리자 입니다. 관리자 ID: {}", member.getId());

    Page<Course> coursePage = courseRepository.findCoursesById(adminId, type, word, pageable);

    if (coursePage.isEmpty()) {
      log.warn("No coursePage found in the database");
      throw new NoSuchElementException("No coursePage found");
    }

    log.info("get {} coursePage from the database", coursePage.getTotalElements());

    // enroll에서 해당 담당자가 맡는 반마다 학생 수 조회
    List<Long> courseIds = coursePage.getContent().stream()
        .map(Course::getId)
        .toList();

    Map<Long, Integer> enrollCounts = enrollRepository.countEnrollsByCourseIds(courseIds);

    // Course 엔티티 리스트를 DTO 리스트로 변환
    List<CourseDTO> dtos = coursePage.getContent().stream()
        .map(course -> {
          Integer counts = enrollCounts.getOrDefault(course.getId(), 0); // 기본값 0
          return fromEntity(course, counts);
        })
        .toList();

    return new PageImpl<>(dtos, pageable, coursePage.getTotalElements());
  }

  /* 교육과정 조회 */
  public CourseDetailDTO getCourseById(long courseId) {
    /*deletdBy가 null이 아니라면 조회하지 않기 추가하기*/
    log.info("get course details for ID: {}", courseId);
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> {
          log.error("Course not found with ID: {}", courseId);
          return new NoSuchElementException("Course not found with id " + courseId);
        });
    log.info("get course details for ID: {}", courseId);

    /* 조회한 course를 CourseDTO 형태로 변환 */
    CourseDTO courseDTO = fromEntity(course, null);

    log.info("변환된 CourseDTO: {}", courseDTO);

    /* 시간표 조회 */
    ScheduleResponseDTO results = scheduleRepository.findScheduleWithPeriodsByCourseId(courseId);

    /*Schedule schedule = null;
    List<PeriodResponseDTO> periods = new ArrayList<>();
    if (results.isEmpty()) {
      log.warn("경고 : 교육과정 ID {}에 대한 시간표가 비어있습니다.", courseId);
      //throw new NoSuchElementException("시간표가 비어있습니다.");
    }else{
      log.info("{} 개의 교시 조회", results.size());

      // Schedule 데이터와 Period 리스트를 추출
      try {
        schedule = results.get(0).get(0, Schedule.class);
        periods = results.stream()
            .map(tuple -> {
                Period period = tuple.get(1,Period.class); // Period 엔티티 가져오기
                return PeriodResponseDTO.fromEntity(period); // DTO로 변환
      }).toList();
      } catch (NullPointerException e) {
        log.error("등록된 시간표가 없습니다.", e);
        throw new RuntimeException("등록된 시간표가 없습니다. courseId: " + courseId, e);
      } catch (IndexOutOfBoundsException | ClassCastException e) {
        log.error("에러 발생: 교육과정 ID {}에 대한 시간표를 찾을 수 없습니다.", courseId);
        throw new RuntimeException("시간표를 찾을 수 없습니다. courseId: " + courseId, e);
      }
    }*/



    /*ScheduleResponseDTO scheduleResponseDTO = schedule != null
    ? ScheduleResponseDTO.builder()
        .id(schedule.getId())
        .courseId(schedule.getCourseId())
        .periods(periods).build()
        : ScheduleResponseDTO.builder()
            .id(null)
            .courseId(null)
            .periods(Collections.emptyList()).build();*/

    log.info("변환된 scheduleResponseDTO: {}", results);

    /*// 수강생 목록
    Page<Member> members = memberRepository.findMemberByCourseId(courseId, pageable);
    Page<MemberDTO> memberList = members.map(member -> MemberDTO.builder()
            .name(member.getName())
            .birthday(member.getBirthday())
            .email(member.getEmail()).build());

    log.info("변환된 List<MemberDTO>: {}", memberList);*/

    return CourseDetailDTO.builder()
        .course(courseDTO)
        .schedule(results)
        .build();
  }

  /* 교육과정 등록 */
  @Transactional
  public CourseDTO createCourse(CourseDTO courseDTO, Long memberId) {
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
    return fromEntity(savedCourse, null);
  }


  /* 교육과정 수정 및 교시 수정 */
  @Transactional
  public CoursePatchResponseDTO updateCourseAndSchedule(Long courseId,
      CoursePatchDTO coursePatchDTO) {
    log.info("Updating course with ID: {}. Patch data: {}", courseId, coursePatchDTO);

    // 1. ID로 Course 조회, 없으면 예외 발생
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> {
          log.error("Course not found with ID: {}", courseId);
          return new NoSuchElementException("Course not found with id " + courseId);
        });

    log.debug("Original course data before update: {}", course);

    // 2. 필드 업데이트
    course.updateWith(coursePatchDTO.getCourse());

    // 3. 업데이트된 Course를 저장
    Course updatedCourse = courseRepository.save(course);

    log.info("Course with ID: {} updated successfully", courseId);
    log.debug("Updated course data: {}", updatedCourse);

    // 4. 업데이트된 엔티티를 DTO로 변환하여 반환
    CourseDTO updatedCourseDTO = fromEntity(updatedCourse, null);

    ScheduleUpdateResponseDTO updatedScheduleDTO = null;
    // 교시 업데이트
    if (coursePatchDTO.getSchedule().getScheduleId() != null) {
      updatedScheduleDTO = scheduleService.updateSchedule(courseId, coursePatchDTO.getSchedule());
    }

    return CoursePatchResponseDTO.builder()
        .course(updatedCourseDTO)
        .schedule(updatedScheduleDTO)
        .build();
  }

  /* 교육과정 삭제 */
  @Transactional
  public void deleteCourse(Long memberId, Long courseId) {
    log.info("교육 과정 ID {} 삭제, 삭제자 {}", courseId, memberId);

    Course existingCourse = courseRepository.findById(courseId)
        .orElseThrow(() -> {
          log.error("Course not found with courseId: {}", courseId);
          return new NoSuchElementException("Course not found with courseId " + courseId);
        });

    // 2. 로그인한 사람의 id를 얻어오기
    existingCourse.updateDeletedBy(memberId);
    // 3. 업데이트된 existingCourse 저장
    courseRepository.save(existingCourse);
    log.info("교육 과정 ID {}, 삭제자 {} 삭제 완료", courseId, memberId);

    // 교시 삭제
    scheduleService.deletePeriod(memberId, courseId);
    log.info("시간표, 교시 삭제 완료");

    // enroll 에서  삭제
    enrollService.deleteEnrollmentByCourseId(memberId, courseId);
    log.info("수강 테이블에서 수강생 목록 삭제 완료");

    log.info("courseId {}에 대한 시간표, 수강신청 목록 삭제 완료", courseId);

  }

  public List<AdminList> getAdminList() {
    log.info("get admin list");
    List<AdminList> adminList = memberRepository.findAdminList()
        .stream()
        .map(member -> new AdminList(member.getId(), member.getName()))
        .toList();

    log.info("admin list: {}", adminList);
    return adminList;
  }

  public Page<MemberDTO> getMembersByCourse(Long courseId, Pageable pageable) {
    log.info("get student list by courseId: {}", courseId);
    return memberRepository.findMemberByCourseId(courseId, pageable)
        .map(member -> MemberDTO.builder()
            .id(member.getId())
            .name(member.getName())
            .birthday(member.getBirthday())
            .email(member.getEmail())
            .build());
  }

  /* 교육과정 조회 */
  public List<CourseDTO> getAvailableCourses() {
    log.info("deletedBy가 null인 교육 과정 조회");
    return courseRepository.findByDeletedByIsNull()
        .stream()
        .map(course -> CourseDTO.fromEntity(course, null)) // studentCounts를 null로 전달
        .toList();
  }

  public List<CourseListDTO> getAllCoursesByAdminId(Long adminId) {
    log.info("adminId :{}",adminId);
    return courseRepository.findCoursesByAdminId(adminId);
  }
}

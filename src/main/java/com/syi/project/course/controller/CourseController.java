package com.syi.project.course.controller;

import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CoursePatchDTO;
import com.syi.project.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/course")
@Slf4j
@RequiredArgsConstructor // final 이 붙은 필드 생성자 자동 주입
@Tag(name = "course", description = "교육과정 API")
public class CourseController {

  private final CourseService courseService;

  /* 교육과정 등록 */
  @Operation(summary = "교육 과정 등록", description = "교육 과정을 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "201", description = "교육 과정이 성공적으로 등록되었습니다."),
      })
  @PostMapping
  public ResponseEntity<CourseDTO> createCourse(
      @Parameter(description = "교육 과정 등록 정보", required = true) @Valid @RequestBody CourseDTO courseDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("Request to create course with data: {}", courseDTO);
    log.debug("교육과정을 등록하려고 로그인 한 사람의 ID: {}", userDetails.getId());
    CourseDTO createdCourse = courseService.createCourse(courseDTO, userDetails.getId());
    log.info("Course created successfully with ID: {}", createdCourse.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
  }

  /* 교육과정 전체 조회 */
  @Operation(summary = "교육 과정 전체 조회", description = "등록된 교육 과정 전체를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "교육 과정이 성공적으로 조회되었습니다."),
      })
  @GetMapping
  public ResponseEntity<Page<CourseDTO>> getAllCourses(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "필터링 타입", required = false)
      @RequestParam(value = "type", required = false) String type,
      @Parameter(description = "검색어", required = false)
      @RequestParam(value = "word",required = false) String word,
      @PageableDefault(size = 10) Pageable pageable
      ) {
    log.info("모든 교육 과정 조회 요청");
    Long memberId = userDetails.getId();
    log.info("관리자 ID: {}", memberId);
    log.debug("모든 교육 과정 조회 요청 - 필터링 타입: {}, 검색어: {}", type, word);
    Page<CourseDTO> courses = courseService.getAllCourses(memberId,type,word,pageable);
    log.info("get {} courses successfully", courses.getTotalElements());
    return ResponseEntity.ok(courses);
  }

  /* 교육과정 상세 조회 */
  @Operation(summary = "교육 과정 상세 조회", description = "등록된 교육 과정을 상세 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "교육 과정이 성공적으로 조회되었습니다."),
      })
  @GetMapping("{id}")
  public ResponseEntity<CourseDTO> getCourseById(@Parameter(description = "상세 조회할 교육과정의 ID", required = true) @PathVariable Long id) {
    log.info("Request to get course with ID: {}", id);
    CourseDTO course = courseService.getCourseById(id);
    log.info("get course with ID: {} successfully", id);
    return ResponseEntity.ok(course);
  }

  @PatchMapping("{id}")
  @Operation(summary = "교육 과정 수정", description = "교육 과정을 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "교육 과정을 성공적으로 수정했습니다."),
      })
  public ResponseEntity<CourseDTO> updateCourse(@Parameter(description = "수정할 교육과정의 ID", required = true) @PathVariable Long id,
      @RequestBody CoursePatchDTO coursePatchDTO) {
    log.info("Request to update course with ID: {}. Update data: {}", id, coursePatchDTO);
    CourseDTO updatedCourse = courseService.updateCourse(id, coursePatchDTO);
    log.info("Course with ID: {} updated successfully", id);
    return ResponseEntity.ok(updatedCourse);
  }

  @DeleteMapping("{id}")
  @Operation(summary = "교육 과정 삭제", description = "교육 과정을 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "교육 과정이 성공적으로 삭제되었습니다."),
      })
  public ResponseEntity<Void> deleteCourse(@Parameter(description = "삭제할 교육과정의 ID", required = true) @PathVariable Long id) {
    log.info("Request to delete course with ID: {}", id);
    courseService.deleteCourse(id);
    log.info("Course with ID: {} deleted successfully", id);
    return ResponseEntity.noContent().build();
  }
}

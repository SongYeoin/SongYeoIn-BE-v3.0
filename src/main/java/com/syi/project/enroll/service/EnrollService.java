package com.syi.project.enroll.service;

import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.enroll.dto.EnrollRequestDTO;
import com.syi.project.enroll.dto.EnrollResponseDTO;
import com.syi.project.enroll.entity.Enroll;
import com.syi.project.enroll.repository.EnrollRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollService {

  private final EnrollRepository enrollRepository;

  // 특정 회원의 수강이력 조회
  @Transactional(readOnly = true)
  public List<EnrollResponseDTO> findEnrollmentsByMemberId(Long memberId) {
    log.info("특정 회원의 수강이력 조회 시작 - memberId: {}", memberId);
    try {
      List<EnrollResponseDTO> enrollments = enrollRepository.findEnrollmentsByMemberId(memberId);
      log.info("회원 수강이력 조회 완료 - memberId: {}, 조회된 수강이력 수: {}",
          memberId, enrollments.size());
      return enrollments;
    } catch (Exception e) {
      log.error("회원 수강이력 조회 중 오류 발생 - memberId: {}", memberId, e);
      throw new InvalidRequestException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 내 수강이력 조회
  @Transactional(readOnly = true)
  public List<EnrollResponseDTO> findMyEnrollments(Long memberId, Role role) {
    log.info("내 수강이력 조회 시작 - memberId: {}, role: {}", memberId, role);

    try {
      if (role == null) {
        log.error("권한 정보 없음 - memberId: {}", memberId);
        throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
      }

      List<EnrollResponseDTO> enrollments;
      if (Role.ADMIN.equals(role)) {
        log.info("관리자 권한으로 전체 교육과정 조회");
        enrollments = enrollRepository.findAllActiveCourses();
      } else {
        log.info("학생 권한으로 개인 수강이력 조회 - memberId: {}", memberId);
        enrollments = enrollRepository.findEnrollmentsByMemberId(memberId);
      }

      log.info("내 수강이력 조회 완료 - memberId: {}, role: {}, 조회된 건수: {}", memberId, role, enrollments.size());
      return enrollments;
    } catch (InvalidRequestException e) {
      throw e;
    } catch (Exception e) {
      log.error("내 수강이력 조회 중 오류 발생 - memberId: {}, role: {}", memberId, role, e);
      throw new InvalidRequestException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 수강신청
  @Transactional
  public EnrollResponseDTO enrollCourse(EnrollRequestDTO requestDTO) {
    log.info("회원 ID와 강의 ID로 수강신청 처리: {} {}", requestDTO.getMemberId(), requestDTO.getCourseId());
    Enroll enroll = requestDTO.toEntity();
    enrollRepository.save(enroll);
    return new EnrollResponseDTO(enroll);
  }

  // 수강신청 삭제
  @Transactional
  public void deleteEnrollment(Long enrollId, Long memberId) {
    log.info("수강신청 ID로 수강신청 삭제: {}, 삭제자 ID: {}", enrollId, memberId);
    if (enrollRepository.findEnrollmentsByMemberId(memberId).stream()
        .noneMatch(enroll -> enroll.getId().equals(enrollId))) {
      throw new InvalidRequestException(ErrorCode.ENROLL_NOT_FOUND);
    }
    enrollRepository.deleteEnrollment(enrollId, memberId);
  }

  // 수강신청 삭제 반 ID로
  @Transactional
  public void deleteEnrollmentByCourseId(Long adminId, Long courseId) {
    log.info("교육과정 ID로 수강 목록 삭제: {}, 삭제자(관리자) ID: {}", courseId, adminId);

    List<Enroll> existingEnroll = enrollRepository.findEnrollmentsByCourseId(courseId);

    // 수강 정보 존재하면 삭제
    if (existingEnroll != null && !existingEnroll.isEmpty()) {
      for (Enroll enroll : existingEnroll) {
        enrollRepository.deleteEnrollmentByCourseId(enroll.getId(), adminId, courseId);
        log.info("1개의 수강신청 목록 삭제");
      }
    }else{
      log.info("해당 courseId {}의 수강 신청이 존재하지 않습니다, 삭제 생략", courseId);
    }

    log.info("courseId {}에 해당하는 수강신청 목록 삭제 완료", courseId);
  }

}

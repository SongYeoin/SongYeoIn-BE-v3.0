package com.syi.project.enroll.service;

import com.syi.project.enroll.dto.EnrollRequestDTO;
import com.syi.project.enroll.dto.EnrollResponseDTO;
import com.syi.project.enroll.entity.Enroll;
import com.syi.project.enroll.repository.EnrollRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollService {

  private final EnrollRepository enrollRepository;
  ;

  // 수강이력 조회
  public List<EnrollResponseDTO> findEnrollmentsByMemberId(Long memberId) {
    log.info("회원 ID로 수강이력 조회: {}", memberId);
    return enrollRepository.findEnrollmentsByMemberId(memberId)
        .stream()
        .map(EnrollResponseDTO::new)
        .collect(Collectors.toList());
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
      throw new EntityNotFoundException("해당 ID의 수강 신청이 존재하지 않습니다: " + enrollId);
    }
    enrollRepository.deleteEnrollment(enrollId, memberId);
  }

}

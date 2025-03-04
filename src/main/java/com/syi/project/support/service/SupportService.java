package com.syi.project.support.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.support.dto.SupportRequestDTO;
import com.syi.project.support.dto.SupportResponseDTO;
import com.syi.project.support.entity.Support;
import com.syi.project.support.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupportService {

  private final SupportRepository supportRepository;
  private final MemberRepository memberRepository;

  // 문의 생성
  @Transactional
  public SupportResponseDTO createSupport(Long memberId, SupportRequestDTO requestDTO) {
    Member member = getMember(memberId);
    Support support = requestDTO.toEntity(member);
    supportRepository.save(support);

    log.info("문의 저장 - id: {}", support.getId());
    return SupportResponseDTO.fromEntity(support);
  }

  // 문의 목록 조회 (학생용 - 자신의 문의만)
  public Page<SupportResponseDTO> getMySupports(Long memberId, Pageable pageable, String keyword) {
    // 새로운 QueryDSL 기반 repository 메서드 사용
    Page<Support> supports = supportRepository.searchSupports(memberId, keyword, pageable);
    return supports.map(SupportResponseDTO::fromEntity);
  }

  // 문의 목록 조회 (관리자용 - 전체)
  public Page<SupportResponseDTO> getAllSupports(Pageable pageable, String keyword) {
    // 관리자는 memberId를 null로 전달하여 모든 문의 조회
    Page<Support> supports = supportRepository.searchSupports(null, keyword, pageable);
    return supports.map(SupportResponseDTO::fromEntity);
  }

  // 페이지네이션 정렬 설정 (작성일, ID 기준 내림차순)
  private Pageable getPageableWithSort(Pageable pageable) {
    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id"))
    );
  }

  // 문의 상세 조회
  public SupportResponseDTO getSupportDetail(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    Member member = getMember(memberId);

    // 관리자가 아니고 작성자도 아닌 경우 접근 불가
    if (!member.getRole().name().equals("ADMIN") && !support.getMember().getId().equals(memberId)) {
      log.error("문의 조회 권한 없음 - id: {}", id);
      throw new InvalidRequestException(ErrorCode.SUPPORT_ACCESS_DENIED);
    }

    return SupportResponseDTO.fromEntity(support);
  }

  // 문의 확인 처리 (관리자용)
  @Transactional
  public SupportResponseDTO confirmSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    support.confirm();
    log.info("문의 확인 처리 완료 - id: {}", id);

    return SupportResponseDTO.fromEntity(support);
  }

  // 문의 확인 처리 취소 (관리자용)
  @Transactional
  public SupportResponseDTO unconfirmSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    support.unconfirm();
    log.info("문의 확인 취소 완료 - id: {}", id);

    return SupportResponseDTO.fromEntity(support);
  }

  // 문의 삭제
  @Transactional
  public void deleteSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndMemberIdAndDeletedByIsNull(id, memberId)
        .orElseThrow(() -> {
          log.error("문의 삭제 권한 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_DELETE_DENIED);
        });

    support.markAsDeleted(memberId);
    log.info("문의 삭제 완료 - id: {}", id);
  }

  // 사용자 정보 호출
  private Member getMember(Long memberId) {
    return memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.error("사용자를 찾을 수 없음 - memberId: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
  }
}
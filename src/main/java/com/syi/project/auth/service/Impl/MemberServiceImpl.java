package com.syi.project.auth.service.Impl;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  // 아이디 중복 검사
  @Override
  public DuplicateCheckDTO checkMemberIdDuplicate(String memberId) {
    log.debug("Member ID 중복 검사 요청: {}", memberId);
    boolean exists = memberRepository.existsByMemberId(memberId);
    String message = exists ? ErrorCode.USER_ALREADY_EXISTS.getMessage() : "사용 가능한 아이디입니다.";
    log.info("아이디 중복 체크: {}, 결과: {}", memberId, message);
    return new DuplicateCheckDTO(!exists, message);
  }

  // 이메일 중복 검사
  @Override
  public DuplicateCheckDTO checkEmailDuplicate(String email) {
    log.debug("Email 중복 검사 요청: {}", email);
    boolean exists = memberRepository.existsByEmail(email);
    String message = exists ? ErrorCode.EMAIL_ALREADY_EXISTS.getMessage() : "사용 가능한 이메일입니다.";
    log.info("이메일 중복 체크: {}, 결과: {}", email, message);
    return new DuplicateCheckDTO(!exists, message);
  }

  // 회원가입
  @Transactional
  @Override
  public MemberSignUpResponseDTO register(MemberSignUpRequestDTO requestDTO) {
    log.info("회원가입 요청: {}", requestDTO.getMemberId());

    if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
      log.warn("비밀번호 불일치 - MemberID: {}", requestDTO.getMemberId());
      throw new InvalidRequestException(ErrorCode.PASSWORD_MISMATCH);
    }

    if (memberRepository.existsByMemberId(requestDTO.getMemberId())) {
      log.warn("이미 사용 중인 MemberID - MemberID: {}", requestDTO.getMemberId());
      throw new InvalidRequestException(ErrorCode.USER_ALREADY_EXISTS);
    }

    if (memberRepository.existsByEmail(requestDTO.getEmail())) {
      log.warn("이미 사용 중인 Email - Email: {}", requestDTO.getEmail());
      throw new InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
    log.debug("비밀번호 인코딩 완료 - MemberID: {}", requestDTO.getMemberId());

    Member member = requestDTO.toEntity(encodedPassword);
    log.debug("회원 엔티티 생성 완료 - MemberID: {}", member.getMemberId());

    Member savedMember = memberRepository.save(member);
    log.info("회원가입 성공: {}", savedMember.getMemberId());

    return new MemberSignUpResponseDTO(
        savedMember.getId(),
        savedMember.getMemberId(),
        savedMember.getName(),
        savedMember.getBirthday(),
        savedMember.getEmail(),
        savedMember.getRole()
    );
  }

  // 로그인
  @Override
  @Transactional
  public MemberLoginResponseDTO login(MemberLoginRequestDTO requestDTO, Role requiredRole) {
    log.info("로그인 검증 시작 - 사용자 ID: {}", requestDTO.getMemberId());

    Member member = memberRepository.findByMemberIdAndIsDeletedFalse(requestDTO.getMemberId())
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(requestDTO.getPassword(), member.getPassword())) {
      log.warn("로그인 실패 - 비밀번호 불일치: 사용자 ID: {}", requestDTO.getMemberId());
      throw new InvalidRequestException(ErrorCode.INVALID_PASSWORD);
    }

    switch (member.getCheckStatus()) {
      case W -> {
        log.warn("로그인 실패 - 승인 대기 상태: 사용자 ID: {}", requestDTO.getMemberId());
        throw new InvalidRequestException(ErrorCode.MEMBER_PENDING_APPROVAL);
      }
      case N -> {
        log.warn("로그인 실패 - 미승인 상태: 사용자 ID: {}", requestDTO.getMemberId());
        throw new InvalidRequestException(ErrorCode.MEMBER_NOT_APPROVED);
      }
    }

    if (member.getRole() != requiredRole) {
      log.warn("로그인 실패 - 권한 없음: 사용자 ID: {}, 요청 역할: {}, 실제 역할: {}",
          requestDTO.getMemberId(), requiredRole, member.getRole());
      throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
    }

    String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    String refreshToken = jwtProvider.createRefreshToken(member.getId());

    log.info("로그인 성공 - 사용자 ID: {}", requestDTO.getMemberId());
    return new MemberLoginResponseDTO(accessToken, refreshToken);
  }

  // 회원목록
  @Override
  public Page<MemberDTO> getFilteredMembers(CheckStatus checkStatus, Role role, Pageable pageable) {
    log.info("필터링된 회원 목록 조회 - 상태: {}, 역할: {}", checkStatus, role);
    Page<Member> members = memberRepository.findByStatusAndRole(checkStatus, role, pageable);
    return members.map(MemberDTO::fromEntity);
  }

  // 회원 상세 조회
  @Override
  public MemberDTO getMemberDetail(String memberId) {
    log.info("회원 상세 정보 조회 - 회원 ID: {}", memberId);
    Member member = memberRepository.findByMemberIdAndIsDeletedFalse(memberId)
        .orElseThrow(() -> {
          log.warn("회원 상세 조회 실패 - 회원 ID: {} (회원 정보 없음)", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
    log.debug("회원 상세 정보 조회 성공 - 회원 ID: {}, 이름: {}", member.getMemberId(), member.getName());
    return MemberDTO.fromEntity(member);
  }


  // Refresh Token 을 이용하여 새로운 Access Token 을 발급하는 메서드
  @Override
  public String refreshToken(String refreshToken) {
    log.info("Refresh Token 검증 시작");

    // Refresh Token 유효성 검사
    if (!jwtProvider.validateRefreshToken(refreshToken)) {
      log.warn("유효하지 않은 Refresh Token: {}", refreshToken);
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // Refresh Token에서 사용자 ID 추출
    Optional<Long> memberIdOpt = jwtProvider.getMemberPrimaryKeyId(refreshToken);
    if (memberIdOpt.isEmpty()) {
      log.error("Refresh Token에서 사용자 ID 추출 실패");
      throw new InvalidRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    Long memberId = memberIdOpt.get();
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
    log.info("새로운 Access Token 발급 완료 - 사용자 ID: {}", memberId);
    return newAccessToken;
  }
}


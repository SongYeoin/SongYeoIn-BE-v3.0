package com.syi.project.auth.service.Impl;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.config.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    String message = exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
    log.info("아이디 중복 체크: {}, 결과: {}", memberId, message);
    return new DuplicateCheckDTO(!exists, message);
  }

  // 이메일 중복 검사
  @Override
  public DuplicateCheckDTO checkEmailDuplicate(String email) {
    log.debug("Email 중복 검사 요청: {}", email);
    boolean exists = memberRepository.existsByEmail(email);
    String message = exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
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
      throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    if (memberRepository.existsByMemberId(requestDTO.getMemberId())) {
      log.warn("이미 사용 중인 MemberID - MemberID: {}", requestDTO.getMemberId());
      throw new IllegalArgumentException("이미 사용 중인 회원 ID입니다.");
    }

    if (memberRepository.existsByEmail(requestDTO.getEmail())) {
      log.warn("이미 사용 중인 Email - Email: {}", requestDTO.getEmail());
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
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
}


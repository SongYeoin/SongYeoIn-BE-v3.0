package com.syi.project.auth.service;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberAdminUpdateRequestDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.dto.MemberUpdateRequestDTO;
import com.syi.project.auth.dto.PasswordResetResponseDTO;
import com.syi.project.auth.entity.JwtBlacklist;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.entity.RefreshToken;
import com.syi.project.auth.repository.JwtBlacklistRepository;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.auth.repository.RefreshTokenRepository;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtBlacklistRepository jwtBlacklistRepository;

  // 아이디 중복 검사
  public DuplicateCheckDTO checkUsernameDuplicate(String username) {
    log.debug("Member Username 중복 검사 요청: {}", username);
    boolean exists = memberRepository.existsByUsername(username);
    String message = exists ? ErrorCode.USER_ALREADY_EXISTS.getMessage() : "사용 가능한 아이디입니다.";
    log.info("아이디 중복 체크: {}, 결과: {}", username, message);
    return new DuplicateCheckDTO(!exists, message);
  }

  // 이메일 중복 검사
  public DuplicateCheckDTO checkEmailDuplicate(String email) {
    log.debug("Email 중복 검사 요청: {}", email);
    boolean exists = memberRepository.existsByEmail(email);
    String message = exists ? ErrorCode.EMAIL_ALREADY_EXISTS.getMessage() : "사용 가능한 이메일입니다.";
    log.info("이메일 중복 체크: {}, 결과: {}", email, message);
    return new DuplicateCheckDTO(!exists, message);
  }

  // 회원가입
  @Transactional
  public MemberSignUpResponseDTO register(MemberSignUpRequestDTO requestDTO) {
    log.info("회원가입 요청: {}", requestDTO.getUsername());

    if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
      log.warn("비밀번호 불일치 - Username: {}", requestDTO.getUsername());
      throw new InvalidRequestException(ErrorCode.PASSWORD_MISMATCH);
    }

    if (memberRepository.existsByUsername(requestDTO.getUsername())) {
      log.warn("이미 사용 중인 Username - Username: {}", requestDTO.getUsername());
      throw new InvalidRequestException(ErrorCode.USER_ALREADY_EXISTS);
    }

    if (memberRepository.existsByEmail(requestDTO.getEmail())) {
      log.warn("이미 사용 중인 Email - Email: {}", requestDTO.getEmail());
      throw new InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
    log.debug("비밀번호 인코딩 완료 - Username: {}", requestDTO.getUsername());

    Member member = requestDTO.toEntity(encodedPassword);
    log.debug("회원 엔티티 생성 완료 - Username: {}", member.getUsername());

    Member savedMember = memberRepository.save(member);
    log.info("회원가입 성공: {}", savedMember.getUsername());

    return new MemberSignUpResponseDTO(
        savedMember.getId(),
        savedMember.getUsername(),
        savedMember.getName(),
        savedMember.getBirthday(),
        savedMember.getEmail(),
        savedMember.getRole()
    );
  }

  // 로그인
  @Transactional
  public MemberLoginResponseDTO login(MemberLoginRequestDTO requestDTO, Role requiredRole, String userAgent, String ipAddress, String deviceFingerprint) {
    log.info("로그인 검증 시작 - 사용자 Username: {}", requestDTO.getUsername());

    Member member = memberRepository.findByUsernameAndDeletedByIsNull(requestDTO.getUsername())
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(requestDTO.getPassword(), member.getPassword())) {
      log.warn("로그인 실패 - 비밀번호 불일치: 사용자 Username: {}", requestDTO.getUsername());
      throw new InvalidRequestException(ErrorCode.INVALID_PASSWORD);
    }

    switch (member.getCheckStatus()) {
      case W -> {
        log.warn("로그인 실패 - 승인 대기 상태: 사용자 Username: {}", requestDTO.getUsername());
        throw new InvalidRequestException(ErrorCode.USER_PENDING_APPROVAL);
      }
      case N -> {
        log.warn("로그인 실패 - 미승인 상태: 사용자 Username: {}", requestDTO.getUsername());
        throw new InvalidRequestException(ErrorCode.USER_NOT_APPROVED);
      }
    }

    if (member.getRole() != requiredRole) {
      log.warn("로그인 실패 - 권한 없음: 사용자 Username: {}, 요청 역할: {}, 실제 역할: {}",
          requestDTO.getUsername(), requiredRole, member.getRole());
      throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
    }

    // 이전 Refresh Token 삭제
    refreshTokenRepository.deleteByMemberId(member.getId());
    log.info("기존 Refresh Token 삭제 완료 - 사용자 ID: {}", member.getId());

    String accessToken = jwtProvider.createAccessToken(member.getId(), member.getName(),
        member.getRole().name(), deviceFingerprint);
    String refreshToken = jwtProvider.createRefreshToken(member.getId());

    // 새 Refresh Token DB에 저장
    refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken,
        jwtProvider.getExpirationDate(refreshToken)));
    log.info("새로운 Refresh Token 저장 완료 - 사용자 ID: {}", member.getId());

    log.info("로그인 성공 - 사용자 Username: {}", requestDTO.getUsername());
    return new MemberLoginResponseDTO(accessToken, refreshToken);
  }

  // 로그아웃
  @Transactional
  public void logout(HttpServletRequest request) {
    String accessToken = extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    String refreshToken = null;

    // HTTP Only 쿠키에서 Refresh Token 가져오기
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("refresh_token".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    // 쿠키에서 찾지 못한 경우 헤더에서 확인
    if (refreshToken == null) {
      refreshToken = extractToken(request.getHeader("Refresh-Token"));
    }

    // Access Token 처리
    if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
      String tokenId = jwtProvider.getJti(accessToken);
      LocalDateTime expiryDate = jwtProvider.getExpirationDate(accessToken);
      jwtBlacklistRepository.save(new JwtBlacklist(tokenId, expiryDate, "ACCESS"));
      log.info("Access Token 블랙리스트 등록 - Token ID: {}, Expiry: {}", tokenId, expiryDate);
    } else {
      log.warn("유효하지 않은 Access Token - 로그아웃 처리 건너뜀");
    }

    // Refresh Token 처리
    if (refreshToken != null && jwtProvider.validateRefreshToken(refreshToken)) {
      jwtProvider.getMemberPrimaryKeyId(refreshToken).ifPresent(refreshTokenRepository::deleteByMemberId);
      log.info("Refresh Token 삭제 완료");
    } else {
      log.warn("유효하지 않은 Refresh Token - 로그아웃 처리 건너뜀");
    }

    // SecurityContext 초기화
    SecurityContextHolder.clearContext();
  }

  private String extractToken(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  // 회원목록
  public Page<MemberDTO> getFilteredMembers(CheckStatus checkStatus, Role role, String word,
      Pageable pageable) {
    log.info("회원 목록 조회 - 상태: {}, 역할: {}, 검색어: {}", checkStatus, role, word);
    Page<Member> members = memberRepository.findByStatusAndRole(checkStatus, role, word, pageable);
    return members.map(MemberDTO::fromEntity);
  }

  // 관리자 특정 회원 상세 조회
  public MemberDTO getMemberDetail(Long id) {
    log.info("회원 상세 정보 조회 - 회원 ID: {}", id);
    Member member = memberRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.warn("회원 상세 조회 실패 - 회원 ID: {} (회원 정보 없음)", id);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
    log.info("회원 상세 정보 조회 성공 - 회원 ID: {}, 이름: {}", member.getId(), member.getName());
    return MemberDTO.fromEntity(member);
  }

  // 승인상태
  @Transactional
  public void updateApprovalStatus(Long id, CheckStatus newStatus) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.warn("회원 승인 상태 업데이트 실패 - 존재하지 않는 회원 ID: {}", id);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    member.updateCheckStatus(newStatus);
    log.info("회원 승인 상태 업데이트 완료 - 회원 ID: {}, 새로운 상태: {}", id, newStatus);
  }

  // 역할
  @Transactional
  public void updateMemberRole(Long id, Role newRole) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.warn("역할 변경 실패 - 회원 정보 없음: 회원 ID: {}", id);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
    member.updateRole(newRole);
    log.info("역할 변경 완료 - 회원 ID: {}, 새로운 역할: {}", id, newRole);
  }
  
  // 회원정보 조회
  public MemberDTO getMemberInfo(Long memberId) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.error("회원정보 조회 실패 - 회원 정보 없음: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    log.info("회원정보 조회 성공 - 회원 ID: {}", memberId);
    return MemberDTO.fromEntity(member);
  }

  // 회원정보 수정
  @Transactional
  public MemberDTO updateMemberInfo(Long memberId, MemberUpdateRequestDTO requestDTO) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    // 현재 비밀번호 검증
    if (requestDTO.getCurrentPassword() != null) {
      if (!passwordEncoder.matches(requestDTO.getCurrentPassword(), member.getPassword())) {
        throw new InvalidRequestException(ErrorCode.INVALID_PASSWORD);
      }
    }

    // 비밀번호 수정
    if (requestDTO.getPassword() != null && requestDTO.getConfirmPassword() != null) {
      if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
        throw new InvalidRequestException(ErrorCode.PASSWORD_MISMATCH);
      }
      member.updatePassword(passwordEncoder.encode(requestDTO.getPassword()));
    }

    // 이메일 수정
    if (requestDTO.getEmail() != null) {
      if (!member.getEmail().equals(requestDTO.getEmail()) &&
          memberRepository.existsByEmail(requestDTO.getEmail())) {
        throw new InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
      }
      member.updateEmail(requestDTO.getEmail());
    }

    return MemberDTO.fromEntity(member);
  }

  // 관리자의 회원정보 수정
  @Transactional
  public MemberDTO updateMemberByAdmin(Long memberId, MemberAdminUpdateRequestDTO requestDTO) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.warn("회원 정보 수정 실패 - 존재하지 않는 회원 ID: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    // 이메일 중복 검사
    if (!member.getEmail().equals(requestDTO.getEmail())) {
      if (memberRepository.existsByEmail(requestDTO.getEmail())) {
        throw new InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
      }
    }

    // 아이디 중복 검사
    if (!member.getUsername().equals(requestDTO.getUsername())) {
      if (memberRepository.existsByUsername(requestDTO.getUsername())) {
        throw new InvalidRequestException(ErrorCode.USER_ALREADY_EXISTS);
      }
    }

    // 각 필드 업데이트
    member.updateByAdmin(
        requestDTO.getName(),
        requestDTO.getUsername(),
        requestDTO.getBirthday(),
        requestDTO.getEmail(),
        requestDTO.getRole(),
        requestDTO.getCheckStatus()
    );

    log.info("관리자에 의한 회원 정보 수정 완료 - 회원 ID: {}", memberId);
    return MemberDTO.fromEntity(member);
  }
  
  // 비밀번호 초기화
  @Transactional
  public PasswordResetResponseDTO resetPassword(Long memberId) {
    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.warn("비밀번호 초기화 실패 - 존재하지 않는 회원 ID: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    String temporaryPassword = generateTemporaryPassword();
    String encodedPassword = passwordEncoder.encode(temporaryPassword);

    member.updatePassword(encodedPassword);
    member.setPasswordChangeRequired(true);

    log.info("회원 비밀번호 초기화 완료 - 회원 ID: {}", memberId);

    return PasswordResetResponseDTO.builder()
        .temporaryPassword(temporaryPassword)
        .resetTime(LocalDateTime.now())
        .build();
  }

  private String generateTemporaryPassword() {
    StringBuilder password = new StringBuilder();
    Random random = new Random();

    // 대문자, 소문자, 숫자, 특수문자 각각 최소 1개 포함
    String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCase = "abcdefghijklmnopqrstuvwxyz";
    String numbers = "0123456789";
    String specialChars = "!@#$%^&*";

    password.append(upperCase.charAt(random.nextInt(upperCase.length())));
    password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
    password.append(numbers.charAt(random.nextInt(numbers.length())));
    password.append(specialChars.charAt(random.nextInt(specialChars.length())));

    // 나머지 6자리는 모든 문자들 중에서 랜덤 선택
    String allChars = upperCase + lowerCase + numbers + specialChars;
    for (int i = 0; i < 6; i++) {
      password.append(allChars.charAt(random.nextInt(allChars.length())));
    }

    // 생성된 비밀번호를 섞음
    char[] passwordArray = password.toString().toCharArray();
    for (int i = passwordArray.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char temp = passwordArray[i];
      passwordArray[i] = passwordArray[j];
      passwordArray[j] = temp;
    }

    return new String(passwordArray);
  }

  // 회원 탈퇴
  @Transactional
  public void deleteMember(Long memberId) {
    log.info("회원 자체 탈퇴 처리 시작 - 회원 ID: {}", memberId);

    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.warn("회원 탈퇴 처리 실패 - 존재하지 않는 회원 ID: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    if (member.getDeletedBy() != null) {
      log.warn("이미 탈퇴 처리된 회원입니다 - 회원 ID: {}", memberId);
      throw new InvalidRequestException(ErrorCode.ALREADY_WITHDRAWN);
    }

    member.deactivate(memberId);
    log.info("회원 자체 탈퇴 처리 완료 - 회원 ID: {}", memberId);
  }

  // 관리자의 회원 탈퇴
  @Transactional
  public void withdrawMember(Long memberId, Long adminId) {
    log.info("관리자에 의한 회원 탈퇴 처리 시작 - 회원 ID: {}, 관리자 ID: {}", memberId, adminId);

    Member member = memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.warn("회원 탈퇴 처리 실패 - 존재하지 않는 회원 ID: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });

    if (member.getDeletedBy() != null) {
      log.warn("이미 탈퇴 처리된 회원입니다 - 회원 ID: {}", memberId);
      throw new InvalidRequestException(ErrorCode.ALREADY_WITHDRAWN);
    }

    member.updateCheckStatus(CheckStatus.N);
    member.deactivate(adminId);

    log.info("관리자에 의한 회원 탈퇴 처리 완료 - 회원 ID: {}, 관리자 ID: {}", memberId, adminId);
  }

}


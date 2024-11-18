package com.syi.project.auth.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceImplTest {

  // @Mock은 Mockito에서 사용하는 어노테이션으로 테스트할 때 가짜 객체를 생성
  // 이 경우 MemberRepository와 PasswordEncoder에 대한 Mock 객체가 생성됨
  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtProvider jwtProvider;

  // @InjectMocks는 MemberServiceImpl 인스턴스에 @Mock으로 선언된 의존성을 주입
  @InjectMocks
  private MemberServiceImpl memberService;

  // mocks는 AutoCloseable 인터페이스를 구현하여
  // @Mock과 @InjectMocks로 초기화한 객체들을 자동으로 닫도록 돕는 객체
  private AutoCloseable mocks;
  private MemberSignUpRequestDTO requestDTO;
  private String encodedPassword;
  private Member member;

  // @BeforeEach는 각 테스트가 실행되기 전에 항상 호출되는 메서드를 나타냄
  // @Mock과 @InjectMocks가 적용된 객체를 초기화
  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);

    // 공통 데이터 생성
    requestDTO = MemberSignUpRequestDTO.builder()
        .memberId("testUser")
        .password("password123")
        .confirmPassword("password123")
        .name("YJ")
        .birthday("1990-01-01")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();
    encodedPassword = "encodedPassword";
    member = new Member("testUser", encodedPassword, "YJ", "1990-01-01", "test@example.com",
        Role.STUDENT);
  }

  // @AfterEach는 각 테스트가 종료된 후 호출되는 메서드를 나타냄
  // AutoCloseable을 구현하는 mocks를 닫아주어 리소스를 해제함
  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  @DisplayName("회원가입 성공 테스트 - 모든 정보가 올바르게 입력된 경우")
  void register_Success() {
    // Mock 설정
    when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn(encodedPassword);
    when(memberRepository.existsByMemberId(requestDTO.getMemberId())).thenReturn(false);
    when(memberRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);

    // 엔티티 저장할 때 변환 검증
    when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
      Member member = invocation.getArgument(0);
      assertEquals(requestDTO.getMemberId(), member.getMemberId());
      assertEquals(encodedPassword, member.getPassword());
      assertEquals(requestDTO.getName(), member.getName());
      assertEquals(requestDTO.getBirthday(), member.getBirthday());
      assertEquals(requestDTO.getEmail(), member.getEmail());
      assertEquals(requestDTO.getRole(), member.getRole());
      return member;
    });

    // When
    MemberSignUpResponseDTO responseDTO = memberService.register(requestDTO);

    // Then
    assertNotNull(responseDTO);
    assertEquals(requestDTO.getMemberId(), responseDTO.getMemberId());
    assertEquals(requestDTO.getName(), responseDTO.getName());
    assertEquals(requestDTO.getBirthday(), responseDTO.getBirthday());
    assertEquals(requestDTO.getEmail(), responseDTO.getEmail());
    assertEquals(requestDTO.getRole(), responseDTO.getRole());
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 비밀번호 불일치")
  void register_Failure_PasswordMismatch() {
    // Given
    MemberSignUpRequestDTO requestDTO = MemberSignUpRequestDTO.builder()
        .memberId("testUser")
        .password("password123")
        .confirmPassword("password456")
        .name("YJ")
        .birthday("1990-01-01")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();

    // When & Then: 비밀번호 불일치 예외 검증
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals(ErrorCode.PASSWORD_MISMATCH.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 중복된 사용자 ID")
  void register_Failure_DuplicateMemberId() {
    when(memberRepository.existsByMemberId(requestDTO.getMemberId())).thenReturn(true);

    // When & Then: 중복된 아이디 예외 발생 검증
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals(ErrorCode.USER_ALREADY_EXISTS.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 중복된 이메일 주소")
  void register_Failure_DuplicateEmail() {
    when(memberRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

    // When & Then: 중복된 이메일 예외 발생 검증
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원 ID 중복 확인 - ID가 존재하는 경우")
  void checkMemberIdDuplicate_IdExists() {
    // Given
    String memberId = "existingUser";
    when(memberRepository.existsByMemberId(memberId)).thenReturn(true);

    // When
    DuplicateCheckDTO result = memberService.checkMemberIdDuplicate(memberId);

    // Then
    assertFalse(result.isAvailable());
    assertEquals("이미 사용 중인 회원 ID입니다.", result.getMessage());
  }

  @Test
  @DisplayName("회원 ID 중복 확인 - ID가 존재하지 않는 경우")
  void checkMemberIdDuplicate_IdNotExists() {
    // Given
    String memberId = "newUser";
    when(memberRepository.existsByMemberId(memberId)).thenReturn(false);

    // When
    DuplicateCheckDTO result = memberService.checkMemberIdDuplicate(memberId);

    // Then
    assertTrue(result.isAvailable());
    assertEquals("사용 가능한 아이디입니다.", result.getMessage());
  }

  @Test
  @DisplayName("이메일 중복 확인 - 이메일이 존재하는 경우")
  void checkEmailDuplicate_EmailExists() {
    // Given
    String email = "existing@example.com";
    when(memberRepository.existsByEmail(email)).thenReturn(true);

    // When
    DuplicateCheckDTO result = memberService.checkEmailDuplicate(email);

    // Then
    assertFalse(result.isAvailable());
    assertEquals("이미 사용 중인 이메일입니다.", result.getMessage());
  }

  @Test
  @DisplayName("이메일 중복 확인 - 이메일이 존재하지 않는 경우")
  void checkEmailDuplicate_EmailNotExists() {
    // Given
    String email = "new@example.com";
    when(memberRepository.existsByEmail(email)).thenReturn(false);

    // When
    DuplicateCheckDTO result = memberService.checkEmailDuplicate(email);

    // Then
    assertTrue(result.isAvailable());
    assertEquals("사용 가능한 이메일입니다.", result.getMessage());
  }


  @Test
  @DisplayName("로그인 성공 테스트 - 승인된 상태")
  void login_Success_Approved() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testUser", "password123");

    member.updateCheckStatus(CheckStatus.Y);

    when(memberRepository.findByMemberIdAndIsDeletedFalse("testUser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(true);
    when(jwtProvider.createAccessToken(member.getId(), member.getRole().name())).thenReturn(
        "accessToken");
    when(jwtProvider.createRefreshToken(member.getId())).thenReturn("refreshToken");

    // When
    MemberLoginResponseDTO responseDTO = memberService.login(requestDTO, Role.STUDENT);

    assertNotNull(responseDTO);
    assertEquals("accessToken", responseDTO.getAccessToken());
    assertEquals("refreshToken", responseDTO.getRefreshToken());
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 승인 대기 상태")
  void login_Failure_PendingApproval() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testUser", "password123");
    member.updateCheckStatus(CheckStatus.W);

    when(memberRepository.findByMemberIdAndIsDeletedFalse("testUser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(true);

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.login(requestDTO, Role.STUDENT)
    );
    assertEquals(ErrorCode.MEMBER_PENDING_APPROVAL.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 미승인 상태")
  void login_Failure_NotApproved() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testUser", "password123");
    member.updateCheckStatus(CheckStatus.N);

    when(memberRepository.findByMemberIdAndIsDeletedFalse("testUser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(true);

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.login(requestDTO, Role.STUDENT)
    );
    assertEquals(ErrorCode.MEMBER_NOT_APPROVED.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
  void login_Failure_WrongPassword() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testUser", "wrongPassword");
    when(memberRepository.findByMemberIdAndIsDeletedFalse("testUser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(false);

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.login(requestDTO, Role.STUDENT)
    );
    assertEquals(ErrorCode.INVALID_PASSWORD.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원 상세 조회 - 성공 케이스")
  void getMemberDetail_Success() {
    when(memberRepository.findByMemberIdAndIsDeletedFalse("testUser")).thenReturn(Optional.of(member));

    MemberDTO result = memberService.getMemberDetail("testUser");

    assertNotNull(result);
    assertEquals("testUser", result.getMemberId());
    assertEquals("YJ", result.getName());
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  @DisplayName("회원 상세 조회 - 존재하지 않는 회원 ID로 조회 시 실패")
  void getMemberDetail_Failure_UserNotFound() {
    when(memberRepository.findByMemberIdAndIsDeletedFalse("nonexistentUser")).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.getMemberDetail("nonexistentUser")
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원 상세 조회 - 삭제된 회원 조회 시 실패")
  void getMemberDetail_Failure_DeletedUser() {
    when(memberRepository.findByMemberIdAndIsDeletedFalse("deletedUser")).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.getMemberDetail("deletedUser")
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("승인 상태 변경 성공 테스트")
  void updateApprovalStatus_Success() {
    String memberId = "testUser";
    CheckStatus newStatus = CheckStatus.Y;

    when(memberRepository.findByMemberIdAndIsDeletedFalse(memberId)).thenReturn(Optional.of(member));

    memberService.updateApprovalStatus(memberId, newStatus);

    assertEquals(newStatus, member.getCheckStatus());
    verify(memberRepository, times(1)).findByMemberIdAndIsDeletedFalse(memberId);
  }

  @Test
  @DisplayName("승인 상태 변경 실패 - 존재하지 않는 회원")
  void updateApprovalStatus_Failure_UserNotFound() {
    String memberId = "nonExistentUser";
    CheckStatus newStatus = CheckStatus.Y;

    when(memberRepository.findByMemberIdAndIsDeletedFalse(memberId)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.updateApprovalStatus(memberId, newStatus)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("역할 변경 성공 테스트")
  void updateMemberRole_Success() {
    String memberId = "testUser";
    Role newRole = Role.ADMIN;

    when(memberRepository.findByMemberIdAndIsDeletedFalse(memberId)).thenReturn(Optional.of(member));

    memberService.updateMemberRole(memberId, newRole);

    assertEquals(newRole, member.getRole());
    verify(memberRepository, times(1)).findByMemberIdAndIsDeletedFalse(memberId);
  }

  @Test
  @DisplayName("역할 변경 실패 - 존재하지 않는 회원")
  void updateMemberRole_Failure_UserNotFound() {
    String memberId = "nonExistentUser";
    Role newRole = Role.ADMIN;

    when(memberRepository.findByMemberIdAndIsDeletedFalse(memberId)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.updateMemberRole(memberId, newRole)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }
}
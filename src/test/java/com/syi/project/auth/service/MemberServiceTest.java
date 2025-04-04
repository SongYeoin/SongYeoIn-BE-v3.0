/*
package com.syi.project.auth.service;

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

class MemberServiceTest {

  // @Mock은 Mockito에서 사용하는 어노테이션으로 테스트할 때 가짜 객체를 생성
  // 이 경우 MemberRepository와 PasswordEncoder에 대한 Mock 객체가 생성됨
  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtProvider jwtProvider;

  // @InjectMocks는 MemberService에 @Mock으로 선언된 의존성을 주입
  @InjectMocks
  private MemberService memberService;

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
        .username("testuser")
        .password("password123")
        .confirmPassword("password123")
        .name("YJ")
        .birthday("1990-01-01")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();
    encodedPassword = "encodedPassword";
    member = new Member("testuser", encodedPassword, "YJ", "1990-01-01", "test@example.com",
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
    when(memberRepository.existsByUsername(requestDTO.getUsername())).thenReturn(false);
    when(memberRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);

    // 엔티티 저장할 때 변환 검증
    when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
      Member member = invocation.getArgument(0);
      assertEquals(requestDTO.getUsername(), member.getUsername());
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
    assertEquals(requestDTO.getUsername(), responseDTO.getUsername());
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
        .username("testuser")
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
  @DisplayName("회원가입 실패 테스트 - 중복된 사용자 Username")
  void register_Failure_DuplicateUsername() {
    when(memberRepository.existsByUsername(requestDTO.getUsername())).thenReturn(true);

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
  @DisplayName("회원 Username 중복 확인 - Username이 존재하는 경우")
  void checkUsernameDuplicate_UsernameExists() {
    // Given
    String username = "existinguser";
    when(memberRepository.existsByUsername(username)).thenReturn(true);

    // When
    DuplicateCheckDTO result = memberService.checkUsernameDuplicate(username);

    // Then
    assertFalse(result.isAvailable());
    assertEquals("이미 사용 중인 회원 ID입니다.", result.getMessage());
  }

  @Test
  @DisplayName("회원 Username 중복 확인 - Username이 존재하지 않는 경우")
  void checkUsernameDuplicate_UsernameNotExists() {
    // Given
    String username = "newuser";
    when(memberRepository.existsByUsername(username)).thenReturn(false);

    // When
    DuplicateCheckDTO result = memberService.checkUsernameDuplicate(username);

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
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testuser", "password123");

    member.updateCheckStatus(CheckStatus.Y);

    when(memberRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(
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
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testuser", "password123");
    member.updateCheckStatus(CheckStatus.W);

    when(memberRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(true);

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.login(requestDTO, Role.STUDENT)
    );
    assertEquals(ErrorCode.USER_PENDING_APPROVAL.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 미승인 상태")
  void login_Failure_NotApproved() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testuser", "password123");
    member.updateCheckStatus(CheckStatus.N);

    when(memberRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(
        Optional.of(member));
    when(passwordEncoder.matches(requestDTO.getPassword(), encodedPassword)).thenReturn(true);

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.login(requestDTO, Role.STUDENT)
    );
    assertEquals(ErrorCode.USER_NOT_APPROVED.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
  void login_Failure_WrongPassword() {
    MemberLoginRequestDTO requestDTO = new MemberLoginRequestDTO("testuser", "wrongPassword");
    when(memberRepository.findByUsernameAndIsDeletedFalse("testuser")).thenReturn(
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
    when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(member));

    MemberDTO result = memberService.getMemberDetail(1L);

    assertNotNull(result);
    assertEquals("YJ", result.getName());
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  @DisplayName("회원 상세 조회 - 존재하지 않는 회원 ID로 조회 시 실패")
  void getMemberDetail_Failure_UserNotFound() {
    when(memberRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.getMemberDetail(10L)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("회원 상세 조회 - 삭제된 회원 조회 시 실패")
  void getMemberDetail_Failure_DeletedUser() {
    when(memberRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.getMemberDetail(1L)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("승인 상태 변경 성공 테스트")
  void updateApprovalStatus_Success() {
    Long id = 1L;
    CheckStatus newStatus = CheckStatus.Y;

    when(memberRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(member));

    memberService.updateApprovalStatus(id, newStatus);

    assertEquals(newStatus, member.getCheckStatus());
    verify(memberRepository, times(1)).findByIdAndIsDeletedFalse(id);
  }

  @Test
  @DisplayName("승인 상태 변경 실패 - 존재하지 않는 회원")
  void updateApprovalStatus_Failure_UserNotFound() {
    Long id = 10L;
    CheckStatus newStatus = CheckStatus.Y;

    when(memberRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.updateApprovalStatus(id, newStatus)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("역할 변경 성공 테스트")
  void updateMemberRole_Success() {
    Long id = 1L;
    Role newRole = Role.ADMIN;

    when(memberRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(member));

    memberService.updateMemberRole(id, newRole);

    assertEquals(newRole, member.getRole());
    verify(memberRepository, times(1)).findByIdAndIsDeletedFalse(id);
  }

  @Test
  @DisplayName("역할 변경 실패 - 존재하지 않는 회원")
  void updateMemberRole_Failure_UserNotFound() {
    Long id = 10L;
    Role newRole = Role.ADMIN;

    when(memberRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> memberService.updateMemberRole(id, newRole)
    );
    assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
  }
}*/

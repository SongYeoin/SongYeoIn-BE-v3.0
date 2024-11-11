package com.syi.project.auth.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

  // @InjectMocks는 MemberServiceImpl 인스턴스에 @Mock으로 선언된 의존성을 주입
  @InjectMocks
  private MemberServiceImpl memberService;

  // mocks는 AutoCloseable 인터페이스를 구현하여
  // @Mock과 @InjectMocks로 초기화한 객체들을 자동으로 닫도록 돕는 객체
  private AutoCloseable mocks;

  // @BeforeEach는 각 테스트가 실행되기 전에 항상 호출되는 메서드를 나타냄
  // 이 메서드는 mocks에 openMocks(this)로 생성된 AutoCloseable 객체를 할당하여
  // @Mock과 @InjectMocks가 적용된 객체를 초기화
  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  // @AfterEach는 각 테스트가 종료된 후 호출되는 메서드를 나타냄
  // AutoCloseable을 구현하는 mocks를 닫아주어 리소스를 해제함
  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void register_Success() {
    // Given
    MemberSignUpRequestDTO requestDTO = MemberSignUpRequestDTO.builder()
        .memberId("testUser")
        .password("password123")
        .confirmPassword("password123")
        .name("YJ")
        .birthday("1990-01-01")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();

    String encodedPassword = "encodedPassword";

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
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals("비밀번호와 비밀번호 확인이 일치하지 않습니다.", exception.getMessage());
  }

  @Test
  void register_Failure_DuplicateMemberId() {
    // Given
    MemberSignUpRequestDTO requestDTO = MemberSignUpRequestDTO.builder()
        .memberId("existingUser")
        .password("password123")
        .confirmPassword("password123")
        .name("YJ")
        .birthday("1990-01-01")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();

    when(memberRepository.existsByMemberId(requestDTO.getMemberId())).thenReturn(true);

    // When & Then: 중복된 아이디 예외 발생 검증
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals("이미 사용 중인 회원 ID입니다.", exception.getMessage());
  }

  @Test
  void register_Failure_DuplicateEmail() {
    // Given
    MemberSignUpRequestDTO requestDTO = MemberSignUpRequestDTO.builder()
        .memberId("newUser")
        .password("password123")
        .confirmPassword("password123")
        .name("YJ")
        .birthday("1990-01-01")
        .email("existing@example.com")
        .role(Role.STUDENT)
        .build();

    when(memberRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

    // When & Then: 중복된 이메일 예외 발생 검증
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> memberService.register(requestDTO)
    );
    assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
  }

  @Test
  void checkMemberIdDuplicate_IdExists() {
    // Given
    String memberId = "existingUser";
    when(memberRepository.existsByMemberId(memberId)).thenReturn(true);

    // When
    DuplicateCheckDTO result = memberService.checkMemberIdDuplicate(memberId);

    // Then
    assertFalse(result.isAvailable());
    assertEquals("이미 사용 중인 아이디입니다.", result.getMessage());
  }

  @Test
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
}
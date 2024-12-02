/*
package com.syi.project.auth.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MemberService memberService;

  @Autowired
  private ObjectMapper objectMapper;  // 요청 및 응답을 JSON 형태로 직렬화/역직렬화

  private MemberSignUpRequestDTO signUpRequestDTO;
  private MemberLoginRequestDTO loginRequestDTO;

  @BeforeEach
  void setUp() {
    // 회원가입 및 로그인 요청 DTO를 설정
    signUpRequestDTO = MemberSignUpRequestDTO.builder()
        .username("testuser")
        .password("password123!")
        .confirmPassword("password123!")
        .name("홍길동")
        .birthday("19900101")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();

    loginRequestDTO = MemberLoginRequestDTO.builder()
        .username("testuser")
        .password("password123!")
        .build();
  }

  @Test
  @DisplayName("회원가입 성공 테스트")
  void register_success() throws Exception {
    // Given
    MemberSignUpResponseDTO signUpResponseDTO = MemberSignUpResponseDTO.builder()
        .id(1L)
        .username("testuser")
        .name("홍길동")
        .birthday("19900101")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();
    Mockito.when(memberService.register(any(MemberSignUpRequestDTO.class)))
        .thenReturn(signUpResponseDTO);

    // When: 회원가입 요청을 JSON 형식으로 직렬화하여 POST 요청 전송
    ResultActions result = mockMvc.perform(post("/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequestDTO)));

    // Then: 기대하는 HTTP 상태 코드와 응답 데이터 확인
    result.andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value(signUpRequestDTO.getUsername()))
        .andExpect(jsonPath("$.name").value(signUpRequestDTO.getName()))
        .andExpect(jsonPath("$.email").value(signUpRequestDTO.getEmail()));
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 비밀번호 불일치")
  void register_passwordMismatch() throws Exception {
    // Given
    signUpRequestDTO = MemberSignUpRequestDTO.builder()
        .username("testuser")
        .password("password123!")
        .confirmPassword("wrongPassword")
        .name("홍길동")
        .birthday("19900101")
        .email("test@example.com")
        .role(Role.STUDENT)
        .build();
    Mockito.when(memberService.register(any(MemberSignUpRequestDTO.class)))
        .thenThrow(new InvalidRequestException(ErrorCode.PASSWORD_MISMATCH));

    ResultActions result = mockMvc.perform(post("/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequestDTO)));

    result.andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value(ErrorCode.PASSWORD_MISMATCH.getMessage()));
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 이미 사용 중인 아이디")
  void register_existingUsername() throws Exception {
    Mockito.when(memberService.register(any(MemberSignUpRequestDTO.class)))
        .thenThrow(new InvalidRequestException(ErrorCode.USER_ALREADY_EXISTS));

    ResultActions result = mockMvc.perform(post("/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequestDTO)));

    result.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_ALREADY_EXISTS.getMessage()));
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 이미 사용 중인 이메일")
  void register_existingEmail() throws Exception {
    Mockito.when(memberService.register(any(MemberSignUpRequestDTO.class)))
        .thenThrow(new InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS));

    ResultActions result = mockMvc.perform(post("/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signUpRequestDTO)));

    result.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
  }

  @Test
  @DisplayName("로그인 성공 테스트 - JWT 토큰 발급")
  void login_success() throws Exception {
    // Given: 로그인 성공 시 반환될 JWT 토큰 응답 DTO 설정
    MemberLoginResponseDTO loginResponseDTO = MemberLoginResponseDTO.builder()
        .accessToken("mockAccessToken")
        .refreshToken("mockRefreshToken")
        .build();
    Mockito.when(memberService.login(any(MemberLoginRequestDTO.class), eq(Role.STUDENT)))
        .thenReturn(loginResponseDTO);

    // When: 로그인 요청을 JSON 형식으로 직렬화하여 POST 요청 전송
    ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDTO)));

    // Then: HTTP 상태 200 (OK)와 JWT 토큰 필드 검증
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
        .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"));
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
  void login_failure_wrongPassword() throws Exception {
    // Given
    loginRequestDTO = MemberLoginRequestDTO.builder()
        .username("testuser")
        .password("wrongPassword")
        .build();
    Mockito.when(memberService.login(any(MemberLoginRequestDTO.class), eq(Role.STUDENT)))
        .thenThrow(new InvalidRequestException(ErrorCode.INVALID_PASSWORD));

    // When
    ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDTO)));

    // Then: HTTP 상태 401 (Unauthorized)와 에러 메시지 검증
    result.andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD.getMessage()));
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 승인 대기 상태")
  void login_failure_pendingApproval() throws Exception {
    Mockito.when(memberService.login(any(MemberLoginRequestDTO.class), eq(Role.STUDENT)))
        .thenThrow(new InvalidRequestException(ErrorCode.USER_PENDING_APPROVAL));

    ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDTO)));

    result.andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_APPROVED.getMessage()));
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 관리자 권한으로 로그인 시 수강생 권한 요구")
  void login_failure_accessDenied() throws Exception {
    loginRequestDTO = MemberLoginRequestDTO.builder()
        .username("username")
        .password("password123!")
        .build();

    Mockito.when(memberService.login(any(MemberLoginRequestDTO.class), eq(Role.STUDENT)))
        .thenThrow(new InvalidRequestException(ErrorCode.ACCESS_DENIED));

    ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequestDTO))
        .with(user("testUser").roles("ADMIN")));  // 관리자 역할로 로그인

    result.andExpect(status().isForbidden())  // 수강생 역할만 허용되므로 403 Forbidden
        .andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
  }

  @Test
  @WithMockUser  // 인증된 사용자로 요청을 보내도록 설정
  @DisplayName("아이디 중복 체크 성공 테스트")
  void checkMemberIdDuplicate_success() throws Exception {
    // Given
    DuplicateCheckDTO duplicateCheckDTO = new DuplicateCheckDTO(true, "사용 가능한 아이디입니다.");
    Mockito.when(memberService.checkUsernameDuplicate("testuser"))
        .thenReturn(duplicateCheckDTO);

    // When
    ResultActions result = mockMvc.perform(get("/check-username")
        .param("username", "testuser"));

    // Then
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.available").value(true))
        .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."));
  }

  @Test
  @WithMockUser
  @DisplayName("이메일 중복 체크 성공 테스트")
  void checkEmailDuplicate_success() throws Exception {
    // Given
    DuplicateCheckDTO duplicateCheckDTO = new DuplicateCheckDTO(true, "사용 가능한 이메일입니다.");
    Mockito.when(memberService.checkEmailDuplicate("test@example.com"))
        .thenReturn(duplicateCheckDTO);

    // When
    ResultActions result = mockMvc.perform(get("/check-email")
        .param("email", "test@example.com"));

    // Then
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.available").value(true))
        .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));
  }

  @Test
  @DisplayName("회원가입 후 로그인하여 JWT 토큰을 발급받기")
  void registerAndLoginTest() throws Exception {
    // 회원가입 요청
    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequestDTO)))
        .andExpect(status().isCreated());

    // 모의 로그인 응답 설정
    MemberLoginResponseDTO loginResponseDTO = MemberLoginResponseDTO.builder()
        .accessToken("mockAccessToken")
        .refreshToken("mockRefreshToken")
        .build();
    Mockito.when(memberService.login(any(MemberLoginRequestDTO.class), eq(Role.STUDENT)))
        .thenReturn(loginResponseDTO);

    // 로그인 요청
    ResultActions result = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());

    // 발급된 토큰을 검증하는 Assertions
    String responseBody = result.andReturn().getResponse().getContentAsString();
    String accessToken = JsonPath.read(responseBody, "$.accessToken");
    String refreshToken = JsonPath.read(responseBody, "$.refreshToken");

    // 토큰 검증 Assertions - 토큰이 null이 아니고 빈 문자열이 아닌지 확인
    assertNotNull(accessToken, "Access Token should not be null");
    assertFalse(accessToken.isEmpty(), "Access Token should not be empty");

    assertNotNull(refreshToken, "Refresh Token should not be null");
    assertFalse(refreshToken.isEmpty(), "Refresh Token should not be empty");
  }

}
*/

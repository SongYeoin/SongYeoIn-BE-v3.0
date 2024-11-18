package com.syi.project.auth.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testUser", roles = "ADMIN")
class AdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MemberService memberService;

  private MemberDTO testMember;

  @BeforeEach
  void setUp() {
    testMember = MemberDTO.builder()
        .id(1L)
        .memberId("testMember")
        .name("Test User")
        .birthday("1990-01-01")
        .email("test@example.com")
        .enrollDate(LocalDate.now())
        .checkStatus(CheckStatus.Y)
        .role(Role.ADMIN)
        .profileUrl("")
        .build();

    Page<MemberDTO> members = new PageImpl<>(List.of(testMember), PageRequest.of(0, 15), 1);
    Mockito.when(memberService.getFilteredMembers(eq(null), eq(null), any(Pageable.class)))
        .thenReturn(members);
  }

  @Test
  @DisplayName("회원 목록 조회 - 모든 회원 조회")
  void getAllMembers_noFilters() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].memberId", is("testMember")))
        .andExpect(jsonPath("$.content[0].name", is("Test User")))
        .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 상태 필터 적용")
  void getAllMembers_withStatusFilter() throws Exception {
    Page<MemberDTO> members = new PageImpl<>(List.of(testMember), PageRequest.of(0, 15), 1);
    Mockito.when(memberService.getFilteredMembers(eq(CheckStatus.Y), eq(null), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("checkStatus", "Y")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].checkStatus", is("Y")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 역할 필터 적용")
  void getAllMembers_withRoleFilter() throws Exception {
    Page<MemberDTO> members = new PageImpl<>(List.of(testMember), PageRequest.of(0, 15), 1);
    Mockito.when(memberService.getFilteredMembers(eq(null), eq(Role.ADMIN), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("role", "ADMIN")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].role", is("ADMIN")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 상태 및 역할 필터 모두 적용")
  void getAllMembers_withStatusAndRoleFilter() throws Exception {
    Page<MemberDTO> members = new PageImpl<>(List.of(testMember), PageRequest.of(0, 15), 1);
    Mockito.when(memberService.getFilteredMembers(eq(CheckStatus.Y), eq(Role.ADMIN), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("checkStatus", "Y")
            .param("role", "ADMIN")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].checkStatus", is("Y")))
        .andExpect(jsonPath("$.content[0].role", is("ADMIN")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 페이징 적용")
  void getAllMembers_withPaging() throws Exception {
    MemberDTO member1 = MemberDTO.builder()
        .id(1L)
        .memberId("member1")
        .name("User One")
        .birthday("1990-01-01")
        .email("user1@example.com")
        .enrollDate(LocalDate.now())
        .checkStatus(CheckStatus.Y)
        .role(Role.STUDENT)
        .profileUrl("")
        .build();

    MemberDTO member2 = MemberDTO.builder()
        .id(2L)
        .memberId("member2")
        .name("User Two")
        .birthday("1991-02-02")
        .email("user2@example.com")
        .enrollDate(LocalDate.now())
        .checkStatus(CheckStatus.Y)
        .role(Role.STUDENT)
        .profileUrl("")
        .build();

    Page<MemberDTO> members = new PageImpl<>(List.of(member1, member2), PageRequest.of(0, 2), 2);
    Mockito.when(memberService.getFilteredMembers(eq(null), eq(null), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("size", "2")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].memberId", is("member1")))
        .andExpect(jsonPath("$.content[1].memberId", is("member2")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 빈 결과")
  void getAllMembers_emptyResult() throws Exception {
    Page<MemberDTO> emptyMembers = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 15), 0);
    Mockito.when(memberService.getFilteredMembers(eq(CheckStatus.N), eq(Role.ADMIN), any(Pageable.class)))
        .thenReturn(emptyMembers);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("checkStatus", "N")
            .param("role", "ADMIN")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @DisplayName("회원 상세 조회 - 성공 케이스")
  void getMemberDetail_Success() throws Exception {
    Mockito.when(memberService.getMemberDetail(eq("testMember"))).thenReturn(testMember);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/detail/testMember")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberId", is("testMember")))
        .andExpect(jsonPath("$.name", is("Test User")))
        .andExpect(jsonPath("$.email", is("test@example.com")));
  }

  @Test
  @DisplayName("회원 상세 조회 - 존재하지 않는 회원 ID로 조회 시 실패")
  void getMemberDetail_Failure_UserNotFound() throws Exception {
    Mockito.when(memberService.getMemberDetail(eq("nonexistentUser")))
        .thenThrow(new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/detail/nonexistentUser")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is(ErrorCode.USER_NOT_FOUND.getMessage())))
        .andExpect(jsonPath("$.status", is(404)));
  }

  @Test
  @DisplayName("회원 상세 조회 - 삭제된 회원 조회 시 실패")
  void getMemberDetail_Failure_DeletedUser() throws Exception {
    Mockito.when(memberService.getMemberDetail(eq("deletedUser")))
        .thenThrow(new InvalidRequestException(ErrorCode.USER_NOT_FOUND));

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/detail/deletedUser")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("해당 사용자가 존재하지 않거나 삭제되었습니다.")))
        .andExpect(jsonPath("$.status", is(404)));
  }

  @Test
  @DisplayName("회원 승인 상태 변경 성공 테스트")
  @WithMockUser(roles = "ADMIN")
  void updateMemberApprovalStatus_success() throws Exception {
    String memberId = "testMember";
    CheckStatus newStatus = CheckStatus.Y;

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/approve/" + memberId)
            .param("newStatus", newStatus.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원 승인 상태 변경 - 회원 정보 없음")
  @WithMockUser(roles = "ADMIN")
  void updateMemberApprovalStatus_memberNotFound() throws Exception {
    String invalidMemberId = "invalidMember";
    CheckStatus newStatus = CheckStatus.Y;

    doThrow(new InvalidRequestException(ErrorCode.USER_NOT_FOUND))
        .when(memberService).updateApprovalStatus(invalidMemberId, newStatus);

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/approve/" + invalidMemberId)
            .param("newStatus", newStatus.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is(ErrorCode.USER_NOT_FOUND.getMessage())));
  }

  @Test
  @DisplayName("회원 승인 상태 변경 - 권한 없음")
  @WithMockUser(roles = "STUDENT")
  void updateMemberApprovalStatus_accessDenied() throws Exception {
    String memberId = "testMember";
    CheckStatus newStatus = CheckStatus.Y;

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/approve/" + memberId)
            .param("newStatus", newStatus.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("역할 변경 성공 테스트")
  void updateMemberRole_Success() throws Exception {
    String memberId = "testMemberId";
    Role newRole = Role.ADMIN;

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/change-role/{memberId}", memberId)
            .param("newRole", newRole.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(memberService, times(1)).updateMemberRole(memberId, newRole);
  }

  @Test
  @DisplayName("역할 변경 실패 - 회원 정보 없음")
  void updateMemberRole_NotFound() throws Exception {
    String memberId = "nonExistingMemberId";
    Role newRole = Role.ADMIN;

    doThrow(new InvalidRequestException(ErrorCode.USER_NOT_FOUND))
        .when(memberService).updateMemberRole(memberId, newRole);

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/change-role/{memberId}", memberId)
            .param("newRole", newRole.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is(ErrorCode.USER_NOT_FOUND.getMessage())))
        .andExpect(jsonPath("$.status", is(404)));

    verify(memberService, times(1)).updateMemberRole(memberId, newRole);
  }

  @Test
  @DisplayName("역할 변경 실패 - 서버 오류 (500 Internal Server Error)")
  void updateMemberRole_InternalServerError() throws Exception {
    String memberId = "testMemberId";
    Role newRole = Role.ADMIN;

    doThrow(new RuntimeException("Unexpected error"))
        .when(memberService).updateMemberRole(memberId, newRole);

    mockMvc.perform(MockMvcRequestBuilders.patch("/admin/change-role/{memberId}", memberId)
            .param("newRole", newRole.name())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())))
        .andExpect(jsonPath("$.status", is(500)));

    verify(memberService, times(1)).updateMemberRole(memberId, newRole);
  }

}

package com.syi.project.auth.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
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
@WithMockUser(username = "testUser", roles = "MANAGER")
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
        .role(Role.MANAGER)
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
    Mockito.when(memberService.getFilteredMembers(eq(null), eq(Role.MANAGER), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("role", "MANAGER")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].role", is("MANAGER")));
  }

  @Test
  @DisplayName("회원 목록 조회 - 상태 및 역할 필터 모두 적용")
  void getAllMembers_withStatusAndRoleFilter() throws Exception {
    Page<MemberDTO> members = new PageImpl<>(List.of(testMember), PageRequest.of(0, 15), 1);
    Mockito.when(memberService.getFilteredMembers(eq(CheckStatus.Y), eq(Role.MANAGER), any(Pageable.class)))
        .thenReturn(members);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("checkStatus", "Y")
            .param("role", "MANAGER")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].checkStatus", is("Y")))
        .andExpect(jsonPath("$.content[0].role", is("MANAGER")));
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
    Mockito.when(memberService.getFilteredMembers(eq(CheckStatus.N), eq(Role.MANAGER), any(Pageable.class)))
        .thenReturn(emptyMembers);

    mockMvc.perform(MockMvcRequestBuilders.get("/admin/list")
            .param("checkStatus", "N")
            .param("role", "MANAGER")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)));
  }
}

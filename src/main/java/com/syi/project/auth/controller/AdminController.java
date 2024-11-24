package com.syi.project.auth.controller;

import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 회원 API", description = "회원가입, 로그인, 회원정보 수정 기능")
public class AdminController {

  private final MemberService memberService;

  @PostMapping("/login")
  @Operation(summary = "관리자 로그인", description = "관리자 로그인 기능입니다. 로그인 후 JWT 토큰을 발급받습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberLoginResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패 - 비밀번호 불일치 또는 권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<MemberLoginResponseDTO> login(
      @Parameter(description = "관리자 로그인 요청 정보", required = true)
      @Valid @RequestBody MemberLoginRequestDTO requestDTO) {
    log.info("관리자 로그인 요청 - 로그인 ID: {}", requestDTO.getUsername());
    MemberLoginResponseDTO responseDTO = memberService.login(requestDTO, Role.ADMIN);
    log.info("관리자 로그인 성공 - 로그인 ID: {}", requestDTO.getUsername());
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  @GetMapping
  @Operation(summary = "회원 목록 조회", description = "회원 상태, 역할에 따른 필터링과 페이징 기능을 지원하는 모든 회원 정보 조회 기능입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<Page<MemberDTO>> getAllMembers(
      @Parameter(description = "회원 상태 필터", required = false)
      @RequestParam(value = "checkStatus", required = false) CheckStatus checkStatus,
      @Parameter(description = "회원 역할 필터", required = false)
      @RequestParam(value = "role", required = false) Role role,
      @Parameter(description = "검색어 (이름)", required = false)
      @RequestParam(value = "word", required = false) String word,
      @PageableDefault(size = 15) Pageable pageable) {
    log.info("회원 목록 조회 요청 - 상태: {}, 역할: {}, 검색어: {}", checkStatus, role, word);
    Page<MemberDTO> members = memberService.getFilteredMembers(checkStatus, role, word, pageable);
    log.info("회원 목록 조회 성공 - 총 {}명", members.getTotalElements());
    return new ResponseEntity<>(members, HttpStatus.OK);
  }

  @GetMapping("/detail/{id}")
  @Operation(summary = "회원 상세 조회", description = "회원 ID를 통해 회원의 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<MemberDTO> getMemberDetail(
      @Parameter(description = "조회할 회원의 ID", required = true)
      @PathVariable Long id) {

    log.info("회원 상세 조회 요청 - 회원 ID: {}", id);
    MemberDTO memberDetail = memberService.getMemberDetail(id);
    return new ResponseEntity<>(memberDetail, HttpStatus.OK);
  }

  @PatchMapping("/approve/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "회원 승인 상태 변경", description = "특정 회원의 승인 여부를 변경합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "승인 상태 변경 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<Void> updateMemberApprovalStatus(
      @Parameter(description = "변경할 회원의 ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "새로운 승인 상태", required = true)
      @RequestParam CheckStatus newStatus) {

    log.info("회원 승인 상태 변경 요청 - 회원 ID: {}, 새로운 상태: {}", id, newStatus);
    memberService.updateApprovalStatus(id, newStatus);
    log.info("회원 승인 상태 변경 성공 - 회원 ID: {}, 새로운 상태: {}", id, newStatus);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/change-role/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "회원 역할 변경", description = "특정 회원의 역할을 변경합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<Void> updateMemberRole(
      @Parameter(description = "변경할 회원의 ID", required = true) @PathVariable Long id,
      @Parameter(description = "새로운 역할", required = true) @RequestParam Role newRole) {

    log.info("회원 역할 변경 요청 - 회원 ID: {}, 새로운 역할: {}", id, newRole);
    memberService.updateMemberRole(id, newRole);
    log.info("회원 역할 변경 성공 - 회원 ID: {}, 새로운 역할: {}", id, newRole);
    return ResponseEntity.ok().build();
  }


}

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 회원 API", description = "회원가입, 로그인, 회원정보 수정 기능")
public class AdminController {

  private final MemberService memberService;

  @PostMapping("/login")
  @Operation(
      summary = "관리자 로그인",
      description = "관리자 로그인 기능입니다. 로그인 후 JWT 토큰을 발급받습니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "로그인 성공",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = MemberLoginResponseDTO.class))),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "401", description = "인증 실패 - 비밀번호 불일치 또는 권한 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  public ResponseEntity<MemberLoginResponseDTO> login(
      @Parameter(description = "관리자 로그인 요청 정보", required = true)
      @Valid @RequestBody MemberLoginRequestDTO requestDTO) {
    log.info("관리자 로그인 요청 - 사용자 ID: {}", requestDTO.getMemberId());
    MemberLoginResponseDTO responseDTO = memberService.login(requestDTO, Role.MANAGER);
    log.info("관리자 로그인 성공 - 사용자 ID: {}", requestDTO.getMemberId());
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  @GetMapping("/list")
  @Operation(
      summary = "회원 목록 조회",
      description = "회원 상태, 역할에 따른 필터링과 페이징 기능을 지원하는 모든 회원 정보 조회 기능입니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  public ResponseEntity<Page<MemberDTO>> getAllMembers(
      @Parameter(description = "회원 상태 필터", required = false)
      @RequestParam(value = "checkStatus", required = false) CheckStatus checkStatus,
      @Parameter(description = "회원 역할 필터", required = false)
      @RequestParam(value = "role", required = false) Role role,
      @PageableDefault(size = 15) Pageable pageable) {

    log.info("회원 목록 조회 요청 - 필터링 상태: {}, 역할: {}", checkStatus, role);
    Page<MemberDTO> members = memberService.getFilteredMembers(checkStatus, role, pageable);
    log.info("회원 목록 조회 성공 - 총 {}명", members.getTotalElements());
    return new ResponseEntity<>(members, HttpStatus.OK);
  }

  @GetMapping("/detail/{memberId}")
  @Operation(
      summary = "회원 상세 조회",
      description = "회원 ID를 통해 회원의 상세 정보를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
      }
  )
  public ResponseEntity<MemberDTO> getMemberDetail(
      @Parameter(description = "조회할 회원의 ID", required = true)
      @PathVariable String memberId) {

    log.info("회원 상세 조회 요청 - 회원 ID: {}", memberId);
    MemberDTO memberDetail = memberService.getMemberDetail(memberId);
    return new ResponseEntity<>(memberDetail, HttpStatus.OK);
  }


}

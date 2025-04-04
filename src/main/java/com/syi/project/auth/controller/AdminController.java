package com.syi.project.auth.controller;

import com.syi.project.auth.dto.MemberAdminUpdateRequestDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.PasswordResetResponseDTO;
import com.syi.project.auth.dto.WithdrawRequestDTO;
import com.syi.project.auth.service.JwtService;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
  private final JwtService jwtService;

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
      @Valid @RequestBody MemberLoginRequestDTO requestDTO,
      @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint,
      HttpServletRequest request,
      HttpServletResponse response) {
    log.info("관리자 로그인 요청 - 로그인 ID: {}", requestDTO.getUsername());

    // 클라이언트 정보 추출
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = jwtService.getClientIp(request);
    MemberLoginResponseDTO responseDTO = memberService.login(requestDTO, Role.ADMIN, userAgent, ipAddress, deviceFingerprint);

    // Refresh Token을 HTTP Only 쿠키로 설정
    Cookie refreshTokenCookie = new Cookie("refresh_token", responseDTO.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true); // 자바스크립트에서 접근 불가능하게 설정
    refreshTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정
    refreshTokenCookie.setPath("/"); // 모든 경로에서 쿠키에 접근 가능하게 설정
    refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유효 (초 단위)
    refreshTokenCookie.setAttribute("SameSite", "Strict"); // SameSite=Strict 설정
    response.addCookie(refreshTokenCookie);

    // 보안 헤더 추가
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");

    log.info("관리자 로그인 성공 - 로그인 ID: {}", requestDTO.getUsername());
    // 응답에는 Access Token만 포함
    return new ResponseEntity<>(
        new MemberLoginResponseDTO(responseDTO.getAccessToken(), null),
        HttpStatus.OK
    );
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
      @PageableDefault(size = 20) Pageable pageable) {
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

  @PostMapping("/reset-password/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "회원 비밀번호 초기화", description = "관리자가 회원의 비밀번호를 임시 비밀번호로 초기화합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<PasswordResetResponseDTO> resetMemberPassword(
      @Parameter(description = "회원 ID", required = true) @PathVariable Long id) {

    log.info("회원 비밀번호 초기화 요청 - 회원 ID: {}", id);
    PasswordResetResponseDTO response = memberService.resetPassword(id);
    log.info("회원 비밀번호 초기화 성공 - 회원 ID: {}", id);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/update/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "관리자의 회원 정보 수정", description = "관리자가 회원의 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<MemberDTO> updateMemberByAdmin(
      @Parameter(description = "수정할 회원의 ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "수정할 회원 정보", required = true)
      @Valid @RequestBody MemberAdminUpdateRequestDTO requestDTO) {

    log.info("관리자의 회원 정보 수정 요청 - 회원 ID: {}", id);
    MemberDTO updatedMember = memberService.updateMemberByAdmin(id, requestDTO);
    log.info("관리자의 회원 정보 수정 완료 - 회원 ID: {}", id);
    return ResponseEntity.ok(updatedMember);
  }

  @PostMapping("/withdraw/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "회원 탈퇴 처리", description = "관리자가 특정 회원을 탈퇴 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원 탈퇴 처리 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<Void> withdrawMember(
      @Parameter(description = "탈퇴 처리할 회원의 ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "탈퇴 처리를 수행한 관리자 정보", required = true,
          content = @Content(schema = @Schema(implementation = WithdrawRequestDTO.class)))
      @Valid @RequestBody WithdrawRequestDTO requestDTO) {

    log.info("회원 탈퇴 처리 요청 - 회원 ID: {}, 처리 관리자 ID: {}", id, requestDTO.getAdminId());
    memberService.withdrawMember(id, requestDTO.getAdminId());
    log.info("회원 탈퇴 처리 성공 - 회원 ID: {}", id);
    return ResponseEntity.ok().build();
  }

}

package com.syi.project.auth.controller;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.dto.MemberUpdateRequestDTO;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.JwtService;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.config.JwtProvider;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "수강생 회원 API", description = "회원가입, 로그인, 회원정보 수정 기능")
public class MemberController {

  private final MemberService memberService;
  private final JwtProvider jwtProvider;
  private final JwtService jwtService;

  @GetMapping("/check-username")
  @Operation(summary = "아이디 중복 체크", description = "아이디 중복 체크를 수행합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "중복 여부 반환")
  })
  public ResponseEntity<DuplicateCheckDTO> checkUsername(
      @Parameter(description = "확인할 회원 아이디", required = true)
      @RequestParam String username) {
    log.info("아이디 중복 체크 요청: {}", username);
    DuplicateCheckDTO response = memberService.checkUsernameDuplicate(username);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/check-email")
  @Operation(summary = "이메일 중복 체크", description = "이메일 중복 체크를 수행합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "중복 여부 반환")
  })
  public ResponseEntity<DuplicateCheckDTO> checkEmail(
      @Parameter(description = "확인할 이메일 주소", required = true)
      @RequestParam String email) {
    log.info("이메일 중복 체크 요청: {}", email);
    DuplicateCheckDTO response = memberService.checkEmailDuplicate(email);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/register")
  @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
  })
  public ResponseEntity<MemberSignUpResponseDTO> register(
      @Parameter(description = "회원가입 요청 정보", required = true)
      @Valid @RequestBody MemberSignUpRequestDTO requestDTO) {
    log.info("회원가입 요청: {}", requestDTO.getUsername());
    MemberSignUpResponseDTO responseDTO = memberService.register(requestDTO);
    return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "수강생 로그인 기능입니다. 로그인 후 JWT 토큰을 발급받습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberLoginResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<MemberLoginResponseDTO> login(
      @Parameter(description = "로그인 요청 정보", required = true)
      @Valid @RequestBody MemberLoginRequestDTO requestDTO,
      @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint,
      HttpServletRequest request,
      HttpServletResponse response) {
    log.info("수강생 로그인 요청 - 로그인 ID: {}", requestDTO.getUsername());

    // 클라이언트 정보 추출
    String userAgent = request.getHeader("User-Agent");
    String ipAddress = jwtService.getClientIp(request);

    MemberLoginResponseDTO responseDTO = memberService.login(
        requestDTO, Role.STUDENT, userAgent, ipAddress, deviceFingerprint);
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

    log.info("수강생 로그인 성공 - 로그인 ID: {}", requestDTO.getUsername());

    // Access Token만 포함된 응답을 반환
    return new ResponseEntity<>(
        new MemberLoginResponseDTO(responseDTO.getAccessToken(), null),
        HttpStatus.OK
    );
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃", description = "로그아웃 기능입니다. Access Token을 블랙리스트에 추가합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    // Refresh Token 쿠키 가져오기
    Cookie[] cookies = request.getCookies();
    String refreshToken = null;

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refresh_token".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    // 헤더가 비어 있는 경우
    if (authHeader == null) {
      log.warn("로그아웃 요청 실패 - Authorization 헤더 정보 부족");
      return ResponseEntity.badRequest().body("로그아웃 실패: Authorization 헤더가 필요합니다.");
    }

    try {
      log.info("로그아웃 요청 - Authorization 헤더 일부: {}, Refresh-Token: {}",
          authHeader != null ? authHeader.substring(0, Math.min(10, authHeader.length())) + "..." : "null",
          refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : "null");

      // 사용자 정보 및 요청 정보 추출
      String token = extractToken(authHeader);
      String userAgent = request.getHeader("User-Agent");
      String ipAddress = jwtService.getClientIp(request);

      // Access Token에서 사용자 ID 추출
      Long memberId = jwtProvider.getMemberPrimaryKeyId(token)
          .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

      // 서비스에 로그아웃 처리 요청 (RefreshToken 제거)
      memberService.logout(request);

      // Refresh Token 쿠키 삭제
      Cookie refreshTokenCookie = new Cookie("refresh_token", null);
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setSecure(true);
      refreshTokenCookie.setPath("/");
      refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 만료
      response.addCookie(refreshTokenCookie);

      log.info("로그아웃 성공 - 사용자 ID: {}", memberId);
      return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    } catch (IllegalArgumentException e) {
      log.error("로그아웃 실패 - 잘못된 요청: {}", e.getMessage());
      return ResponseEntity.badRequest().body("로그아웃 실패: " + e.getMessage());
    } catch (Exception e) {
      log.error("로그아웃 실패 - 서버 오류: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그아웃 실패: 서버 오류");
    }
  }

  @GetMapping("/info")
  @Operation(summary = "회원정보 조회", description = "로그인된 회원의 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원정보 조회 성공"),
      @ApiResponse(responseCode = "401", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음")
  })
  public ResponseEntity<MemberDTO> getMemberInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    MemberDTO memberDTO = memberService.getMemberInfo(memberId);
    return ResponseEntity.ok(memberDTO);
  }

  @PatchMapping("/update")
  @Operation(summary = "회원정보 수정", description = "비밀번호와 이메일을 수정한 후 변경된 정보를 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음")
  })
  public ResponseEntity<MemberDTO> updateMemberInfo(
      @Valid @RequestBody MemberUpdateRequestDTO requestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getId();
    MemberDTO updatedMember = memberService.updateMemberInfo(memberId, requestDTO);
    return ResponseEntity.ok(updatedMember);
  }

  @DeleteMapping("/delete")
  @Operation(summary = "회원탈퇴", description = "로그인된 사용자가 자신의 계정을 탈퇴합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
      @ApiResponse(responseCode = "401", description = "권한 없음")
  })
  public ResponseEntity<Void> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    memberService.deleteMember(memberId);
    return ResponseEntity.ok().build();
  }

  private String extractToken(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

}
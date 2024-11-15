package com.syi.project.auth.controller;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.auth.service.MemberService;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.InvalidRequestException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "수강생 회원 API", description = "회원가입, 로그인, 회원정보 수정 기능")
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/check-id")
  @Operation(summary = "아이디 중복 체크", description = "아이디 중복 체크를 수행합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "중복 여부 반환")
  })
  public ResponseEntity<DuplicateCheckDTO> checkMemberId(
      @Parameter(description = "확인할 회원 아이디", required = true)
      @RequestParam String memberId) {
    log.info("아이디 중복 체크 요청: {}", memberId);
    DuplicateCheckDTO response = memberService.checkMemberIdDuplicate(memberId);
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
    log.info("회원가입 요청: {}", requestDTO.getMemberId());
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
      @Valid @RequestBody MemberLoginRequestDTO requestDTO) {
    log.info("수강생 로그인 요청 - 사용자 ID: {}", requestDTO.getMemberId());
    MemberLoginResponseDTO responseDTO = memberService.login(requestDTO, Role.STUDENT);
    log.info("수강생 로그인 성공 - 사용자 ID: {}", requestDTO.getMemberId());
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

}

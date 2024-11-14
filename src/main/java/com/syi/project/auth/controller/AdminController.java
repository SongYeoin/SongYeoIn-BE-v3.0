package com.syi.project.auth.controller;

import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.service.MemberService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}

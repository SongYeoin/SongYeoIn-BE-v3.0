package com.syi.project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  MEMBER_PENDING_APPROVAL("E001", "아직 승인되지 않은 회원입니다.", HttpStatus.FORBIDDEN),
  MEMBER_NOT_APPROVED("E002", "미승인된 회원입니다. 관리자에게 문의하세요.", HttpStatus.FORBIDDEN),
  PASSWORD_MISMATCH("E003", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD("E004", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  USER_NOT_FOUND("E005", "해당 사용자가 존재하지 않거나 삭제되었습니다.", HttpStatus.NOT_FOUND),
  ACCESS_DENIED("E006", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  INVALID_REFRESH_TOKEN("E007", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
  USER_ALREADY_EXISTS("E008", "이미 사용 중인 회원 ID입니다.", HttpStatus.BAD_REQUEST),
  EMAIL_ALREADY_EXISTS("E009", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
  VALIDATION_FAILED("E010", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("E011", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  public String getStatusReason() {
    return httpStatus.getReasonPhrase();
  }
}

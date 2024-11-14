package com.syi.project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  MEMBER_PENDING_APPROVAL("아직 승인되지 않은 회원입니다.", HttpStatus.FORBIDDEN),
  MEMBER_NOT_APPROVED("미승인된 회원입니다. 관리자에게 문의하세요.", HttpStatus.FORBIDDEN),
  PASSWORD_MISMATCH("비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  USER_NOT_FOUND("해당 사용자가 존재하지 않거나 삭제되었습니다.", HttpStatus.NOT_FOUND),
  ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
  USER_ALREADY_EXISTS("이미 사용 중인 회원 ID입니다.", HttpStatus.BAD_REQUEST),
  EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String message;
  private final HttpStatus httpStatus;

  // 생성자
  ErrorCode(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

  // HTTP 상태 코드의 이유 문구를 반환
  public String getStatusReason() {
    return httpStatus.getReasonPhrase();
  }
}

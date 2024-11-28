package com.syi.project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // User 관련 에러
  USER_PENDING_APPROVAL("USER_001", "아직 승인되지 않은 회원입니다.", HttpStatus.FORBIDDEN),
  USER_NOT_APPROVED("USER_002", "미승인된 회원입니다. 관리자에게 문의하세요.", HttpStatus.FORBIDDEN),
  USER_NOT_FOUND("USER_003", "해당 사용자가 존재하지 않거나 삭제되었습니다.", HttpStatus.NOT_FOUND),
  USER_ALREADY_EXISTS("USER_004", "이미 사용 중인 회원 ID입니다.", HttpStatus.BAD_REQUEST),
  EMAIL_ALREADY_EXISTS("USER_005", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
  ACCESS_DENIED("USER_006", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  USER_UPDATE_FAILED("USER_007", "회원정보 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  EMAIL_DUPLICATE_ON_UPDATE("USER_008", "수정하려는 이메일이 이미 사용 중입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_INVALID_ON_UPDATE("USER_009", "수정하려는 비밀번호가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

  // 비밀번호 관련 에러
  PASSWORD_MISMATCH("AUTH_001", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD("AUTH_002", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

  // 인증/인가 관련 에러
  INVALID_REFRESH_TOKEN("AUTH_003", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
  // 강의 관련 에러
  COURSE_NOT_FOUND("COURSE_001", "요청한 교육과정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 공지사항 관련 에러
  NOTICE_NOT_FOUND("NOTICE_001", "요청한 공지사항을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOTICE_UPDATE_DENIED("NOTICE_002", "공지사항 수정 권한이 없습니다.", HttpStatus.FORBIDDEN),
  NOTICE_DELETE_DENIED("NOTICE_003", "공지사항 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // 파일 관련 에러
  INVALID_FILE_FORMAT("FILE_001", "허용되지 않은 파일 형식입니다.", HttpStatus.BAD_REQUEST),
  FILE_UPLOAD_FAILED("FILE_002", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  FILE_MIME_TYPE_MISMATCH("FILE_003", "파일 MIME 타입이 허용되지 않습니다.", HttpStatus.BAD_REQUEST),
  FILE_EXTENSION_MISMATCH("FILE_004", "파일 확장자가 허용되지 않습니다.", HttpStatus.BAD_REQUEST),

  // 유효성 검증 관련 에러
  VALIDATION_FAILED("COMMON_001", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

  // 시스템 관련 에러
  INTERNAL_SERVER_ERROR("SYSTEM_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // 동아리 관련 에러
  CANNOT_MODIFY_APPROVED("CLUB_001", "승인된 상태에서는 수정할 수 없습니다.", HttpStatus.FORBIDDEN),
  CANNOT_MODIFY_PENDING("CLUB_002", "미승인된 상태에서는 수정할 수 없습니다.", HttpStatus.FORBIDDEN),
  CANNOT_DELETE_APPROVED("CLUB_003", "대기상태에서만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN),
  INVALID_REQUEST("CLUB_004", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);


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

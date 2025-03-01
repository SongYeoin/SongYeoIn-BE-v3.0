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
  ALREADY_WITHDRAWN("USER_010", "이미 탈퇴 처리된 회원입니다.", HttpStatus.BAD_REQUEST),

  // 비밀번호 관련 에러
  PASSWORD_MISMATCH("AUTH_001", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD("AUTH_002", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

  // 인증/인가 관련 에러
  INVALID_REFRESH_TOKEN("AUTH_003", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("AUTH_004", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
  SECURITY_RISK_DETECTED("AUTH_005", "보안 위험이 감지되었습니다. 계정을 보호하기 위해 모든 토큰이 무효화되었습니다.", HttpStatus.UNAUTHORIZED),

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
  FILE_NOT_FOUND("FILE_005", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  FILE_ACCESS_DENIED("FILE_006", "파일에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  FILE_DOWNLOAD_FAILED("FILE_007", "파일 다운로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  FILE_NOT_IN_STORAGE("FILE_008", "저장소에서 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  FILE_INVALID_STATE("FILE_009", "파일이 유효하지 않은 상태입니다.", HttpStatus.BAD_REQUEST),
  FILE_COUNT_EXCEEDED("FILE_010", "파일 개수 초과", HttpStatus.BAD_REQUEST),

  // 유효성 검증 관련 에러
  VALIDATION_FAILED("COMMON_001", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

  // 시스템 관련 에러
  INTERNAL_SERVER_ERROR("SYSTEM_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // 동아리 관련 에러
  CANNOT_MODIFY_APPROVED("CLUB_001", "승인된 상태에서는 수정할 수 없습니다.", HttpStatus.FORBIDDEN),
  CANNOT_MODIFY_PENDING("CLUB_002", "미승인된 상태에서는 수정할 수 없습니다.", HttpStatus.FORBIDDEN),
  CANNOT_DELETE_APPROVED("CLUB_003", "대기상태에서만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN),
  INVALID_REQUEST("CLUB_004", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

  // 교육일지 관련 에러
  JOURNAL_NOT_FOUND("JOURNAL_001", "요청한 교육일지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  JOURNAL_ACCESS_DENIED("JOURNAL_002", "교육일지에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  JOURNAL_DUPLICATE_DATE("JOURNAL_003", "해당 날짜에 이미 작성된 교육일지가 있습니다.", HttpStatus.BAD_REQUEST),
  JOURNAL_INVALID_DATE("JOURNAL_004", "유효하지 않은 교육일자입니다.", HttpStatus.BAD_REQUEST),
  JOURNAL_DATE_OUT_OF_RANGE("JOURNAL_007", "교육일자가 과정 기간을 벗어났습니다.", HttpStatus.BAD_REQUEST),
  JOURNAL_INVALID_FILE_TYPE("JOURNAL_008", "교육일지는 hwp, hwpx, doc, docx 형식만 첨부 가능합니다.", HttpStatus.BAD_REQUEST),
  JOURNAL_FILE_NOT_FOUND("JOURNAL_009", "선택된 교육일지 중 파일이 없는 항목이 있습니다.", HttpStatus.BAD_REQUEST),
  JOURNAL_DOWNLOAD_FAILED("JOURNAL_010", "교육일지 일괄 다운로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // 출석 관련 에러
  // 공통
  ATTENDANCE_NOT_IN_RANGE("ATTENDANCE_001", "학원 네트워크에서만 출석이 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_FAILED("ATTENDANCE_002", "출석에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  // 입실
  ATTENDANCE_ENTRY_NOT_ALLOWED("ATTENDANCE_ENTRY_001", "입실이 가능한 시간이 아닙니다", HttpStatus.FORBIDDEN),
  ATTENDANCE_ENTRY_TOO_EARLY("ATTENDANCE_ENTRY_002", "입실 가능한 시간이 아닙니다. 교시 시작 40분 전부터 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_ENTRY_TOO_LATE("ATTENDANCE_ENTRY_003", "입실 가능한 시간이 아닙니다. 마지막 교시 시작 후 20분 전까지 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_ALREADY_ENTERED("ATTENDANCE_ENTRY_004","이미 입실했습니다.", HttpStatus.BAD_REQUEST),
  ATTENDANCE_ENTRY_NOT_FIND_PERIOD("ATTENDANCE_ENTRY_005","입실 가능한 교시가 없습니다.",HttpStatus.BAD_REQUEST),

  // 퇴실
  ATTENDANCE_EXIT_NOT_ALLOWED("ATTENDANCE_EXIT_001", "퇴실 가능 시간이 아닙니다. 마지막 교시 종료 전 20분부터 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_EXIT_TOO_EARLY("ATTENDANCE_EXIT_002", "퇴실 시간이 너무 이릅니다. 교시 종료 10분 전부터 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_EXIT_TOO_LATE("ATTENDANCE_EXIT_003", "퇴실 시간이 너무 늦었습니다. 교시 종료 후 10분 후까지 가능합니다.", HttpStatus.FORBIDDEN),
  ATTENDANCE_ALREADY_EXITED("ATTENDANCE_EXIT_004","이미 퇴실했습니다.", HttpStatus.BAD_REQUEST),
  ATTENDANCE_ENTRY_NOT_FOUND("ATTENDANCE_EXIT_005","입실한 기록이 없습니다.",HttpStatus.BAD_REQUEST),
  ATTENDANCE_EXIT_NOT_FIND_PERIOD("ATTENDANCE_EXIT_006","퇴실 가능한 교시가 없습니다.",HttpStatus.BAD_REQUEST),

  // 조퇴
  ATTENDANCE_EARLY_EXIT_ALREADY_HAS_STATUS("ATTENDANCE_EARLY_EXIT_001","이미 출석한 교시에는 조퇴할 수 없습니다.",HttpStatus.BAD_REQUEST),

  // 출석률 관련
  ATTENDANCE_SEGMENT_NOT_FOUND("ATTENANCE_SEGMENT_001","해당하는 차수를 찾을 수 없습니다.",HttpStatus.NOT_FOUND),

  // 프린트 관련
  ATTENDANCE_PRINT_DATA_NOT_FOUND("ATTENDANCE_PRINT_001","해당 차수에 대한 프린트 데이터가 없습니다",HttpStatus.NOT_FOUND),

  // 수강신청 관련 에러
  ENROLL_NOT_FOUND("ENROLL_001", "수강 이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ENROLL_ACCESS_DENIED("ENROLL_002", "수강 이력에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // 고객센터 관련 에러
  SUPPORT_NOT_FOUND("SUPPORT_001", "요청한 문의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  SUPPORT_ACCESS_DENIED("SUPPORT_002", "문의에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  SUPPORT_DELETE_DENIED("SUPPORT_003", "문의 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);


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

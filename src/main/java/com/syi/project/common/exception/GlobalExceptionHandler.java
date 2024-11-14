package com.syi.project.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

  // InvalidRequestException 예외 처리
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
      InvalidRequestException ex) {
    log.error("InvalidRequestException 발생: {}", ex.getMessage());
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("status", ex.getHttpStatus().value());
    errorResponse.put("error", ex.getHttpStatus().getReasonPhrase());
    return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
  }

  // 특정 예외를 처리하는 핸들러 메서드
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    log.error("RuntimeException 발생: {}", ex.getMessage());
    ErrorCode errorCode = getErrorCodeFromMessage(ex.getMessage());
    return new ResponseEntity<>(buildErrorResponse(errorCode), errorCode.getHttpStatus());
  }

  // 유효성 검사 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.warn("ValidationException 발생: {}", ex.getMessage());
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }

    ErrorCode errorCode = ErrorCode.PASSWORD_MISMATCH; // 예시로 PASSWORD_MISMATCH를 사용
    Map<String, Object> response = buildErrorResponse(errorCode);
    response.put("fieldErrors", fieldErrors);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 404 예외 처리
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
      NoHandlerFoundException ex) {
    log.warn("NoHandlerFoundException 발생: {}", ex.getMessage());
    ErrorCode errorCode = ErrorCode.USER_NOT_FOUND; // 예시로 USER_NOT_FOUND 사용
    return new ResponseEntity<>(buildErrorResponse(errorCode), errorCode.getHttpStatus());
  }

  // 일반적인 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
    log.error("Unexpected exception 발생: {}", ex.getMessage(), ex);
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR; // 예시로 INTERNAL_SERVER_ERROR 사용
    return new ResponseEntity<>(buildErrorResponse(errorCode), errorCode.getHttpStatus());
  }

  // 예외 메시지에 맞는 ErrorCode를 반환하는 메서드
  private ErrorCode getErrorCodeFromMessage(String exceptionMessage) {
    switch (exceptionMessage) {
      case "비밀번호와 비밀번호 확인이 일치하지 않습니다.":
        return ErrorCode.PASSWORD_MISMATCH;
      case "이미 사용 중인 회원 ID입니다.":
        return ErrorCode.USER_ALREADY_EXISTS;
      case "이미 사용 중인 이메일입니다.":
        return ErrorCode.EMAIL_ALREADY_EXISTS;
      case "비밀번호가 일치하지 않습니다.":
        return ErrorCode.INVALID_PASSWORD;
      case "접근 권한이 없습니다.":
        return ErrorCode.ACCESS_DENIED;
      case "해당 사용자가 존재하지 않거나 삭제되었습니다.":
        return ErrorCode.USER_NOT_FOUND;
      case "유효하지 않은 Refresh Token입니다.":
        return ErrorCode.INVALID_REFRESH_TOKEN;
      default:
        return ErrorCode.INTERNAL_SERVER_ERROR; // 기본 에러 처리
    }
  }

  // 공통 오류 응답 빌드
  private Map<String, Object> buildErrorResponse(ErrorCode errorCode) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", errorCode.getHttpStatus().value());
    errorResponse.put("error", errorCode.getStatusReason());
    errorResponse.put("message", errorCode.getMessage());
    return errorResponse;
  }
}

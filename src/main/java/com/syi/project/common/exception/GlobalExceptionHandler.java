package com.syi.project.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.SemanticException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
@RestController
public class GlobalExceptionHandler {

  @ExceptionHandler(SemanticException.class)
  public ResponseEntity<?> handleQueryDslException(SemanticException ex) {
    log.error("QueryDSL 오류 발생: {}", ex.getMessage());
    return ResponseEntity.badRequest().body("잘못된 요청입니다: " + ex.getMessage());
  }


  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  // InvalidRequestException 예외 처리
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
      InvalidRequestException ex) {
    log.error("InvalidRequestException 발생: {}", ex.getMessage());
    return new ResponseEntity<>(buildErrorResponse(ex.getErrorCode()), ex.getHttpStatus());
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

    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
    Map<String, Object> response = buildErrorResponse(errorCode);
    response.put("fieldErrors", fieldErrors);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 404 예외 처리
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
    log.warn("404 Not Found 발생 - Request URL: {}, Request Method: {}", ex.getRequestURL(),
        ex.getHttpMethod());

    // 404 전용 에러 코드 사용 또는 생성 필요
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
    errorResponse.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
    errorResponse.put("message", "요청하신 페이지를 찾을 수 없습니다.");
    errorResponse.put("path", ex.getRequestURL());

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // 일반적인 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
    log.error("Unexpected exception 발생: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR),
        ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
  }

  // 공통 오류 응답 빌드
  private Map<String, Object> buildErrorResponse(ErrorCode errorCode) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", errorCode.getHttpStatus().value());
    errorResponse.put("error", errorCode.getStatusReason());
    errorResponse.put("code", errorCode.getCode());
    errorResponse.put("message", errorCode.getMessage());
    return errorResponse;
  }

}
package com.syi.project.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
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

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);  // 404 상태 코드와 메시지 반환
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
  public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
      NoHandlerFoundException ex) {
    log.warn("NoHandlerFoundException 발생: {}", ex.getMessage());
    return new ResponseEntity<>(buildErrorResponse(ErrorCode.USER_NOT_FOUND),
        ErrorCode.USER_NOT_FOUND.getHttpStatus());
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
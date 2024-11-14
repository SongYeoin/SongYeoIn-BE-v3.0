package com.syi.project.common.exception;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);  // 404 상태 코드와 메시지 반환
  }
}

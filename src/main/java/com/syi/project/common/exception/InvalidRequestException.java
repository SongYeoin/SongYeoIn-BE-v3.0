package com.syi.project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidRequestException extends RuntimeException {

  private final HttpStatus httpStatus;

  // ErrorCode를 받는 생성자 추가
  public InvalidRequestException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.httpStatus = errorCode.getHttpStatus();
  }
}
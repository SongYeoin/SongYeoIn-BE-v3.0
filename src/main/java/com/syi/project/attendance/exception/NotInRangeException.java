package com.syi.project.attendance.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotInRangeException extends RuntimeException {

  private final HttpStatus status;

  public NotInRangeException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

}
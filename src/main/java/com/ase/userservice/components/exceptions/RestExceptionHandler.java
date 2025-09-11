package com.ase.userservice.components.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;

@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex, req);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex, req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ex, req);
  }

  private ResponseEntity<ApiError> build(HttpStatus status, Exception ex, HttpServletRequest req) {
    ApiError error = new ApiError(
        req.getRequestURI(),
        status.value(),
        status.getReasonPhrase(),
        ex.getMessage(),
        OffsetDateTime.now()
    );
    return ResponseEntity.status(status).body(error);
  }
}

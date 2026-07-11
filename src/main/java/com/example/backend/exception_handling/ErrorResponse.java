package com.example.backend.exception_handling;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors,
    LocalDateTime timestamp) {

  public static ErrorResponse of(int status, String message) {
    return new ErrorResponse(status, message, null, LocalDateTime.now());
  }

  public static ErrorResponse of(int status, String message, Map<String, String> errors) {

    return new ErrorResponse(status, message, errors, LocalDateTime.now());
  }
}

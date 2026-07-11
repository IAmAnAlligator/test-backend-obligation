package com.example.backend.exception_handling;

public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }
}

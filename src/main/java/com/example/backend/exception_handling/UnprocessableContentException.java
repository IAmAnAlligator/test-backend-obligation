package com.example.backend.exception_handling;

public class UnprocessableContentException extends BusinessException {

  public UnprocessableContentException(String message) {
    super(message);
  }
}

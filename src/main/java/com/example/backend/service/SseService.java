package com.example.backend.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

  SseEmitter subscribe();

  void send(String eventName, Object data);
}

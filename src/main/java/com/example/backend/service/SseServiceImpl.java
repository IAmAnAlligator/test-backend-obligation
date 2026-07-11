package com.example.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter subscribe() {

    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    emitters.add(emitter);

    try {
      emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
    } catch (IOException e) {
      emitters.remove(emitter);
    }

    emitter.onCompletion(() -> emitters.remove(emitter));

    emitter.onTimeout(() -> emitters.remove(emitter));

    emitter.onError(error -> emitters.remove(emitter));

    return emitter;
  }

  public void send(String eventName, Object data) {

    emitters.forEach(
        emitter -> {
          try {

            emitter.send(SseEmitter.event().name(eventName).data(data));

          } catch (IOException e) {

            emitters.remove(emitter);
          }
        });
  }
}

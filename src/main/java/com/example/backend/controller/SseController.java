package com.example.backend.controller;

import com.example.backend.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("api/obligations/events")
@RequiredArgsConstructor
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(
      summary = "Подписаться на SSE события",
      description =
          """
          Открывает SSE соединение.
          Клиент получает события сервера в формате text/event-stream.
          Поддерживаемое событие:
          - obligation_deleted — отправляется после удаления обязательства.
          """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "SSE соединение успешно установлено",
        content =
            @Content(
                examples =
                    @ExampleObject(value = """
        SSE connection established
        """))),
    @ApiResponse(
        responseCode = "500",
        description = "Ошибка создания SSE соединения",
        content = @Content(examples = @ExampleObject(value = """

        """)))
  })
  public SseEmitter subscribe() {

    return sseService.subscribe();
  }
}

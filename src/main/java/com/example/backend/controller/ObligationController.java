package com.example.backend.controller;

import com.example.backend.dto.CreateObligationResponse;
import com.example.backend.dto.ObligationCreateRequest;
import com.example.backend.dto.ObligationDto;
import com.example.backend.dto.PayObligationResponse;
import com.example.backend.dto.PayRequest;
import com.example.backend.dto.UpcomingObligationsResponse;
import com.example.backend.enums.Category;
import com.example.backend.enums.Status;
import com.example.backend.service.ObligationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/obligations")
@RequiredArgsConstructor
@Tag(name = "Обязательства", description = "Операции с обязательствами")
public class ObligationController {

  private final ObligationService obligationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Создать обязательство", description = "Создаёт новое обязательство")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Обязательство создано",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
            {
              "obligation": {
                "id": "ed6c7ced-6b0d-44bd-9755-431adf26d1f9",
                "title": "Netflix",
                "amount": 12.99,
                "currency": "EUR",
                "category": "SUBSCRIPTION",
                "recurrence": "MONTHLY",
                "next_payment_date": "2026-08-15",
                "status": "ACTIVE",
                "created_at": "2026-07-11T14:05:07.311251108",
                "updated_at": "2026-07-11T14:05:07.311278974"
              }
            }
            """))),
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка валидации",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
{
    "status": 400,
    "message": "Validation failed",
    "errors": {
        "title": "Title is required"
    },
    "timestamp": "2026-07-11T14:16:38.614835321"
}
            """)))
  })
  public CreateObligationResponse createObligation(
      @RequestBody @Valid ObligationCreateRequest request) {
    return obligationService.createObligation(request);
  }

  @GetMapping
  @Operation(
      summary = "Возвратить список обязательств",
      description =
          "Возвращает список обязательств. Принимает опциональные query-параметры category и status, "
              + "поддерживает их одновременное применение. Результат отсортирован по next_payment_date по возрастанию.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Список обязательств успешно возвращён",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
[{
        "id": "9c574be4-a4fa-447e-b5bf-9a923bfe2166",
        "title": "YouTube",
        "amount": 100.00,
        "currency": "USD",
        "category": "SUBSCRIPTION",
        "recurrence": "MONTHLY",
        "next_payment_date": "2026-07-15",
        "status": "ACTIVE",
        "created_at": "2026-07-11T14:21:17.526836",
        "updated_at": "2026-07-11T14:21:17.526836"
    },
    {
        "id": "8533f365-4738-40b9-a862-24e2a32bef86",
        "title": "Netflix",
        "amount": 500.00,
        "currency": "EUR",
        "category": "SUBSCRIPTION",
        "recurrence": "YEARLY",
        "next_payment_date": "2027-01-31",
        "status": "ACTIVE",
        "created_at": "2026-07-11T14:20:26.778811",
        "updated_at": "2026-07-11T14:20:26.778811"
    }
]
           \s"""))),
    @ApiResponse(
        responseCode = "400",
        description = "Некорректное значение параметров",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """

            {
                "status": 400,
                "message": "Invalid value 'other' for enum Category",
                "timestamp": "2026-07-11T14:24:50.420056506"
            }

            """)))
  })
  public List<ObligationDto> getObligations(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status) {

    return obligationService.getObligations(
        parseEnum(Category.class, category), parseEnum(Status.class, status));
  }

  @GetMapping("/upcoming")
  @Operation(
      summary = "Возвращает обязательства",
      description =
          "Возвращает обязательства с next_payment_date в диапазоне [today, today + N days]. "
              + "Параметр days — "
              + "целое число, по умолчанию 7.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Обязательства успешно возвращены",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
        {
            "obligations": [
                {
                    "id": "d5754b13-3f98-4967-aab7-fa8b86b6b921",
                    "title": "YouTube",
                    "amount": 100.00,
                    "currency": "USD",
                    "category": "SUBSCRIPTION",
                    "recurrence": "MONTHLY",
                    "next_payment_date": "2026-07-15",
                    "status": "ACTIVE",
                    "created_at": "2026-07-11T14:46:39.88577",
                    "updated_at": "2026-07-11T14:46:39.88577"
                },
                {
                    "id": "e7de6476-be58-40f8-a0c6-d81d8ec8d036",
                    "title": "Netflix",
                    "amount": 1500.00,
                    "currency": "USD",
                    "category": "SUBSCRIPTION",
                    "recurrence": "YEARLY",
                    "next_payment_date": "2026-07-16",
                    "status": "ACTIVE",
                    "created_at": "2026-07-11T14:49:48.465508",
                    "updated_at": "2026-07-11T14:49:48.465508"
                }
            ],
            "totals": {
                "USD": 1600.00
            },
            "renewal_alerts": [
                {
                    "id": "d5754b13-3f98-4967-aab7-fa8b86b6b921",
                    "title": "YouTube",
                    "next_payment_date": "2026-07-15",
                    "amount": 100.00,
                    "currency": "USD"
                },
                {
                    "id": "e7de6476-be58-40f8-a0c6-d81d8ec8d036",
                    "title": "Netflix",
                    "next_payment_date": "2026-07-16",
                    "amount": 1500.00,
                    "currency": "USD"
                }
            ]
        }
        """))),
  })
  public UpcomingObligationsResponse getUpcoming(
      @Parameter(description = "Количество дней для поиска", example = "7")
          @RequestParam(defaultValue = "7")
          int days) {
    return obligationService.getUpcoming(days);
  }

  @PostMapping("/{id}/pay")
  @Operation(
      summary = "Оплатить обязательство",
      description = "Фиксирует факт оплаты и обновляет обязательство")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Обязательство успешно оплачено",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """

        {
            "obligation": {
                "id": "2d196d47-9d06-4034-8895-c12fbd9029a3",
                "title": "YouTube",
                "amount": 100.00,
                "currency": "USD",
                "category": "SUBSCRIPTION",
                "recurrence": "MONTHLY",
                "next_payment_date": "2026-08-15",
                "status": "ACTIVE",
                "created_at": "2026-07-11T14:29:07.203501",
                "updated_at": "2026-07-11T14:29:33.923685022"
            },
            "payment": {
                "id": "fc369f79-8b27-47d9-973f-100e7853a7ad",
                "obligation_id": "2d196d47-9d06-4034-8895-c12fbd9029a3",
                "amount": 500,
                "currency": "USD",
                "paid_at": "2026-07-11T14:29:33.919516223"
            }
        }

        """))),
    @ApiResponse(
        responseCode = "422",
        description = "Статус обязательства не active",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """

        {
            "status": 422,
            "message": "Only active obligations can be paid",
            "timestamp": "2026-07-11T14:32:46.135755249"
        }


        """)))
  })
  public PayObligationResponse pay(
      @Parameter(
              description = "UUID обязательства",
              example = "b3a9e79b-05b9-4a5c-bac1-cf046d4a7b08")
          @PathVariable
          UUID id,
      @RequestBody @Valid PayRequest request) {
    return obligationService.pay(id, request.amount());
  }

  @PatchMapping("/{id}/cancel")
  @Operation(
      summary = "Отменить обязательство",
      description = "Переводит обязательство в статус cancelled. " + "Запись остаётся в базе")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Обязательство успешно отменено",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """

        {
            "id": "3eb8fd79-1956-4940-8bc3-3cb9154ca09f",
            "title": "Netflix",
            "amount": 100.00,
            "currency": "USD",
            "category": "SUBSCRIPTION",
            "recurrence": "MONTHLY",
            "next_payment_date": "2026-07-15",
            "status": "CANCELLED",
            "created_at": "2026-07-11T14:35:31.034806",
            "updated_at": "2026-07-11T14:35:47.747793497"
        }

        """))),
    @ApiResponse(
        responseCode = "422",
        description = "Статус обязательства не active",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
        {
            "status": 422,
            "message": "Only active obligations can be cancelled",
            "timestamp": "2026-07-11T14:38:45.051504389"
        }

        """)))
  })
  @ResponseStatus(HttpStatus.OK)
  public ObligationDto cancel(
      @Parameter(
              description = "UUID обязательства",
              example = "b3a9e79b-05b9-4a5c-bac1-cf046d4a7b08")
          @PathVariable
          UUID id) {
    return ObligationDto.from(obligationService.cancel(id));
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Удалить обязательство",
      description = "Удаляет обязательство и все связанные с ним записи")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Обязательство успешно удалено"),
    @ApiResponse(
        responseCode = "404",
        description = "Обязательство не найдено",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
          {
              "status": 404,
              "message": "Obligation with id b3a9e79b-05b9-4a5c-bac1-cf046d4a7b08 not found",
              "timestamp": "2026-07-11T14:41:05.29121617"
          }
          """)))
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(
              description = "UUID обязательства",
              example = "b3a9e79b-05b9-4a5c-bac1-cf046d4a7b08")
          @PathVariable
          UUID id) {
    obligationService.delete(id);
  }

  private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {

    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));

    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid value '%s' for enum %s".formatted(value, enumClass.getSimpleName()));
    }
  }
}

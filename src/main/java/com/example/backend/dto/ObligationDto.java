package com.example.backend.dto;

import com.example.backend.entity.Obligation;
import com.example.backend.enums.Category;
import com.example.backend.enums.Recurrence;
import com.example.backend.enums.Status;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ObligationDto(
    UUID id,
    String title,
    BigDecimal amount,
    String currency,
    Category category,
    Recurrence recurrence,
    LocalDate nextPaymentDate,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static ObligationDto from(Obligation obligation) {
    return new ObligationDto(
        obligation.getId(),
        obligation.getTitle(),
        obligation.getAmount(),
        obligation.getCurrency(),
        obligation.getCategory(),
        obligation.getRecurrence(),
        obligation.getNextPaymentDate(),
        obligation.getStatus(),
        obligation.getCreatedAt(),
        obligation.getUpdatedAt());
  }
}

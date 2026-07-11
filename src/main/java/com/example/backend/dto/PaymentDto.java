package com.example.backend.dto;

import com.example.backend.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDto(
    UUID id, UUID obligationId, BigDecimal amount, String currency, LocalDateTime paidAt) {

  public static PaymentDto from(Payment payment) {
    return new PaymentDto(
        payment.getId(),
        payment.getObligation().getId(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getPaidAt());
  }
}

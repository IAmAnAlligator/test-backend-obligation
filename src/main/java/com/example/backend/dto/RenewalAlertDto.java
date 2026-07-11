package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RenewalAlertDto(
    UUID id, String title, LocalDate nextPaymentDate, BigDecimal amount, String currency) {}

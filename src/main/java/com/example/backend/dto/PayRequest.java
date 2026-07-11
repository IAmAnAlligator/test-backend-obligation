package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PayRequest(
    @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Amount must have maximum 2 decimal places")
        @Schema(description = "Payment amount", example = "15.99")
        BigDecimal amount) {}

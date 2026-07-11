package com.example.backend.dto;

import com.example.backend.enums.Category;
import com.example.backend.enums.Recurrence;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ObligationCreateRequest(
    @Schema(description = "Obligation title", example = "Netflix subscription")
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    @Schema(description = "Payment amount", example = "15.99")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Amount must have maximum 2 decimal places")
        BigDecimal amount,
    @Schema(description = "Currency code", example = "USD")
        @NotBlank(message = "Currency is required")
        @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be a valid ISO 4217 code (e.g. USD, EUR)")
        String currency,
    @Schema(description = "Obligation category", example = "subscription")
        @NotNull(message = "Category is required")
        Category category,
    @Schema(description = "Payment recurrence", example = "monthly") Recurrence recurrence,
    @Schema(description = "Next payment date", example = "2026-08-15")
        @JsonProperty("next_payment_date")
        @NotNull(message = "Next payment date is required")
        LocalDate nextPaymentDate) {}

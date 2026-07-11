package com.example.backend.util;

import com.example.backend.dto.ObligationCreateRequest;
import com.example.backend.entity.Obligation;
import com.example.backend.enums.Category;
import com.example.backend.enums.Recurrence;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TestDataFactory {

  public static ObligationCreateRequest createRequest() {
    return new ObligationCreateRequest(
        "Netflix",
        BigDecimal.valueOf(500),
        "USD",
        Category.SUBSCRIPTION,
        Recurrence.MONTHLY,
        LocalDate.now().plusDays(5));
  }

  public static Obligation activeMonthly() {
    return Obligation.create(
        "Netflix",
        BigDecimal.valueOf(500),
        "USD",
        Category.SUBSCRIPTION,
        Recurrence.MONTHLY,
        LocalDate.now().plusDays(10));
  }

  public static Obligation activeQuarterly() {
    return Obligation.create(
        "Insurance",
        BigDecimal.valueOf(1000),
        "USD",
        Category.INSURANCE,
        Recurrence.QUARTERLY,
        LocalDate.now().plusDays(10));
  }

  public static Obligation activeYearly() {
    return Obligation.create(
        "Warranty",
        BigDecimal.valueOf(2000),
        "USD",
        Category.WARRANTY,
        Recurrence.YEARLY,
        LocalDate.now().plusDays(10));
  }

  public static Obligation activeWithoutRecurrence() {
    return Obligation.create(
        "Electricity",
        BigDecimal.valueOf(300),
        "USD",
        Category.BILL,
        null,
        LocalDate.now().plusDays(5));
  }

  public static Obligation expiredNonRecurring() {

    Obligation obligation =
        Obligation.create(
            "Old Bill",
            BigDecimal.valueOf(100),
            "USD",
            Category.BILL,
            null,
            LocalDate.now().minusDays(5));

    obligation.expire();

    return obligation;
  }

  public static Obligation expiredRecurring() {
    return Obligation.create(
        "Spotify",
        BigDecimal.valueOf(400),
        "USD",
        Category.SUBSCRIPTION,
        Recurrence.MONTHLY,
        LocalDate.now().minusDays(5));
  }

  public static Obligation january31Monthly() {
    return Obligation.create(
        "Netflix",
        BigDecimal.valueOf(500),
        "USD",
        Category.SUBSCRIPTION,
        Recurrence.MONTHLY,
        LocalDate.of(2026, 1, 31));
  }

  public static Obligation leapYearJanuary31() {
    return Obligation.create(
        "Netflix",
        BigDecimal.valueOf(500),
        "USD",
        Category.SUBSCRIPTION,
        Recurrence.MONTHLY,
        LocalDate.of(2028, 1, 31));
  }
}

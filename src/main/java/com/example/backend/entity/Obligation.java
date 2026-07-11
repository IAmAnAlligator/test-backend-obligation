package com.example.backend.entity;

import com.example.backend.enums.Category;
import com.example.backend.enums.Recurrence;
import com.example.backend.enums.Status;
import com.example.backend.exception_handling.UnprocessableContentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "obligations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Obligation {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  @UuidGenerator
  private UUID id;

  @Column(name = "title", nullable = false)
  private String title;

  @Positive
  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence")
  private Recurrence recurrence;

  @Column(name = "next_payment_date", nullable = false)
  private LocalDate nextPaymentDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "obligation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Payment> payments = new ArrayList<>();

  public void addPayment(Payment payment) {
    payments.add(payment);
  }

  public static Obligation create(
      String title,
      BigDecimal amount,
      String currency,
      Category category,
      Recurrence recurrence,
      LocalDate nextPaymentDate) {

    validate(title, amount, currency, category, nextPaymentDate);

    LocalDateTime now = LocalDateTime.now();

    return new Obligation(
        null,
        title,
        amount,
        currency,
        category,
        recurrence,
        nextPaymentDate,
        Status.ACTIVE,
        now,
        now,
        new ArrayList<>());
  }

  // =========================
  // DOMAIN BEHAVIOR
  // =========================

  public void expire() {
    if (this.status != Status.EXPIRED) {
      this.status = Status.EXPIRED;
      touch();
    }
  }

  public void cancel() {

    if (this.status != Status.ACTIVE) {
      throw new UnprocessableContentException("Only active obligations can be cancelled");
    }

    this.status = Status.CANCELLED;
    touch();
  }

  public void reschedule(LocalDate newDate) {
    this.nextPaymentDate = newDate;
    touch();
  }

  public void activate() {
    this.status = Status.ACTIVE;
    touch();
  }

  public Payment pay(BigDecimal amount) {

    if (this.status != Status.ACTIVE) {
      throw new IllegalStateException("Only active obligations can be paid");
    }

    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Amount must be greater than 0");
    }

    Payment payment = Payment.create(this, amount, this.currency);

    addPayment(payment);

    touch();

    return payment;
  }

  // =========================
  // INTERNAL HELPERS
  // =========================

  private void touch() {
    this.updatedAt = LocalDateTime.now();
  }

  private static void validate(
      String title,
      BigDecimal amount,
      String currency,
      Category category,
      LocalDate nextPaymentDate) {

    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title is required");
    }

    if (amount == null || amount.signum() < 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }

    Objects.requireNonNull(currency, "Currency is required");
    Objects.requireNonNull(category, "Category is required");
    Objects.requireNonNull(nextPaymentDate, "Next payment date is required");
  }

  // =========================
  // IDENTITY
  // =========================

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Obligation that)) {
      return false;
    }

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

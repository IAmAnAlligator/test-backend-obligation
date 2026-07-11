package com.example.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.example.backend.enums.Category;
import com.example.backend.enums.Recurrence;
import com.example.backend.enums.Status;
import com.example.backend.exception_handling.UnprocessableContentException;
import com.example.backend.util.TestDataFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ObligationTest {

  @Test
  void shouldCreatePayment() {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment payment = obligation.pay(BigDecimal.valueOf(500));

    assertNotNull(payment);

    assertEquals(obligation, payment.getObligation());

    assertEquals(BigDecimal.valueOf(500), payment.getAmount());

    assertEquals("USD", payment.getCurrency());

    assertNotNull(payment.getPaidAt());
  }

  @Test
  void shouldThrowWhenAmountIsZero() {

    Obligation obligation = TestDataFactory.activeMonthly();

    assertThrows(IllegalArgumentException.class, () -> obligation.pay(BigDecimal.ZERO));
  }

  @Test
  void shouldThrowWhenAmountIsNegative() {

    Obligation obligation = TestDataFactory.activeMonthly();

    assertThrows(IllegalArgumentException.class, () -> obligation.pay(BigDecimal.valueOf(-10)));
  }

  @Test
  void shouldNotPayExpiredObligation() {

    Obligation obligation = TestDataFactory.expiredNonRecurring();

    assertThrows(IllegalStateException.class, () -> obligation.pay(BigDecimal.valueOf(100)));
  }

  @Test
  void shouldNotPayCancelledObligation() {

    Obligation obligation = TestDataFactory.activeWithoutRecurrence();

    obligation.cancel();

    assertThrows(IllegalStateException.class, () -> obligation.pay(BigDecimal.valueOf(100)));
  }

  @Test
  void shouldCancelActiveObligation() {

    Obligation obligation = TestDataFactory.activeWithoutRecurrence();

    obligation.cancel();

    assertEquals(Status.CANCELLED, obligation.getStatus());
  }

  @Test
  void shouldNotCancelExpiredObligation() {

    Obligation obligation = TestDataFactory.expiredNonRecurring();

    assertThrows(UnprocessableContentException.class, obligation::cancel);

    assertEquals(Status.EXPIRED, obligation.getStatus());
  }

  @Test
  void shouldExpireActiveObligation() {

    Obligation obligation = TestDataFactory.activeWithoutRecurrence();

    obligation.expire();

    assertEquals(Status.EXPIRED, obligation.getStatus());
  }

  @Test
  void shouldActivateExpiredObligation() {

    Obligation obligation = TestDataFactory.expiredNonRecurring();

    obligation.activate();

    assertEquals(Status.ACTIVE, obligation.getStatus());
  }

  @Test
  void shouldReschedulePaymentDate() {

    Obligation obligation = TestDataFactory.activeMonthly();

    LocalDate newDate = LocalDate.now().plusMonths(2);

    obligation.reschedule(newDate);

    assertEquals(newDate, obligation.getNextPaymentDate());
  }

  @Test
  void shouldUpdateUpdatedAtAfterStateChange() {

    Obligation obligation = TestDataFactory.activeMonthly();

    var before = obligation.getUpdatedAt();

    obligation.cancel();

    assertTrue(
        obligation.getUpdatedAt().isAfter(before) || obligation.getUpdatedAt().isEqual(before));
  }

  @Test
  void shouldNotBeEqualToNull() {

    Obligation obligation = TestDataFactory.activeMonthly();

    assertNotEquals(null, obligation);
  }

  @Test
  void shouldNotBeEqualToAnotherType() {

    Obligation obligation = TestDataFactory.activeMonthly();

    assertNotEquals("test", obligation);
  }

  @Test
  void shouldCreateActiveObligation() {

    Obligation obligation =
        Obligation.create(
            "Netflix",
            BigDecimal.valueOf(500),
            "USD",
            Category.SUBSCRIPTION,
            Recurrence.MONTHLY,
            LocalDate.now().plusDays(10));

    assertEquals("Netflix", obligation.getTitle());

    assertEquals(BigDecimal.valueOf(500), obligation.getAmount());

    assertEquals(Status.ACTIVE, obligation.getStatus());

    assertEquals(Recurrence.MONTHLY, obligation.getRecurrence());
  }

  @Test
  void shouldRemainActiveWhenCreatedWithPastDate() {

    Obligation obligation =
        Obligation.create(
            "Electricity",
            BigDecimal.valueOf(100),
            "USD",
            Category.BILL,
            null,
            LocalDate.now().minusDays(5));

    assertEquals(Status.ACTIVE, obligation.getStatus());
  }

  @Test
  void shouldThrowWhenTitleIsBlank() {

    assertThrows(
        IllegalArgumentException.class,
        () ->
            Obligation.create(
                "", BigDecimal.valueOf(100), "USD", Category.BILL, null, LocalDate.now()));
  }

  @Test
  void shouldBeEqualWhenObligationsHaveSameId() throws Exception {

    Obligation first = TestDataFactory.activeMonthly();

    Obligation second = TestDataFactory.activeMonthly();

    UUID id = UUID.randomUUID();

    setId(first, id);
    setId(second, id);

    assertEquals(first, second);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenObligationsHaveDifferentIds() throws Exception {

    Obligation first = TestDataFactory.activeMonthly();

    Obligation second = TestDataFactory.activeMonthly();

    setId(first, UUID.randomUUID());
    setId(second, UUID.randomUUID());

    assertNotEquals(first, second);
  }

  @Test
  void shouldNotBeEqualWhenObligationIdIsNull() {

    Obligation first = TestDataFactory.activeMonthly();

    Obligation second = TestDataFactory.activeMonthly();

    assertNotEquals(first, second);
  }

  @Test
  void shouldHaveSameHashCodeForObligations() {

    Obligation first = TestDataFactory.activeMonthly();

    Obligation second = TestDataFactory.activeWithoutRecurrence();

    assertEquals(first.hashCode(), second.hashCode());
  }

  private static void setId(Obligation obligation, UUID id) throws Exception {

    Field field = Obligation.class.getDeclaredField("id");

    field.setAccessible(true);

    field.set(obligation, id);
  }
}

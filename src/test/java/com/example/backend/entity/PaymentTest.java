package com.example.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.example.backend.util.TestDataFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentTest {

  @Test
  void shouldBeEqualWhenPaymentsHaveSameId() throws Exception {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment first = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    Payment second = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    UUID id = UUID.randomUUID();

    setId(first, id);
    setId(second, id);

    assertEquals(first, second);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenPaymentsHaveDifferentIds() throws Exception {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment first = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    Payment second = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    setId(first, UUID.randomUUID());
    setId(second, UUID.randomUUID());

    assertNotEquals(first, second);
  }

  @Test
  void shouldNotBeEqualToNull() {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment payment = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    assertNotEquals(null, payment);
  }

  @Test
  void shouldNotBeEqualToAnotherType() {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment payment = Payment.create(obligation, BigDecimal.valueOf(500), "USD");

    assertNotEquals("payment", payment);
  }

  @Test
  void shouldHaveSameHashCodeForPayments() {

    Obligation obligation = TestDataFactory.activeMonthly();

    Payment first = Payment.create(obligation, BigDecimal.valueOf(100), "USD");

    Payment second = Payment.create(obligation, BigDecimal.valueOf(200), "EUR");

    assertEquals(first.hashCode(), second.hashCode());
  }

  private static void setId(Payment payment, UUID id) throws Exception {

    Field field = Payment.class.getDeclaredField("id");

    field.setAccessible(true);

    field.set(payment, id);
  }
}

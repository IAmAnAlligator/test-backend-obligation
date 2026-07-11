package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.CreateObligationResponse;
import com.example.backend.dto.ObligationCreateRequest;
import com.example.backend.dto.ObligationDto;
import com.example.backend.entity.Obligation;
import com.example.backend.enums.Category;
import com.example.backend.enums.Status;
import com.example.backend.repository.ObligationRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.util.TestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObligationServiceImplTest {

  @Mock private ObligationRepository obligationRepository;

  @Mock private PaymentRepository paymentRepository;

  @Mock private SseService sseService;

  @InjectMocks private ObligationServiceImpl service;

  private Obligation obligation;

  @BeforeEach
  void setUp() {

    obligation = TestDataFactory.activeMonthly();
  }

  // =====================================================
  // CREATE
  // =====================================================

  @Test
  void shouldCreateObligationWithoutWarning() {

    ObligationCreateRequest request = TestDataFactory.createRequest();

    when(obligationRepository.existsByStatusAndTitleIgnoreCase(Status.ACTIVE, request.title()))
        .thenReturn(false);

    when(obligationRepository.save(any(Obligation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateObligationResponse response = service.createObligation(request);

    assertNotNull(response);

    assertNull(response.warning());

    assertEquals(request.title(), response.obligation().title());

    verify(obligationRepository).save(any(Obligation.class));
  }

  // 5. Создание дубля + warning

  @Test
  void shouldReturnWarningWhenDuplicateExists() {

    ObligationCreateRequest request = TestDataFactory.createRequest();

    when(obligationRepository.existsByStatusAndTitleIgnoreCase(Status.ACTIVE, request.title()))
        .thenReturn(true);

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    CreateObligationResponse response = service.createObligation(request);

    assertEquals("Активное обязательство с таким названием уже существует", response.warning());
  }

  // =====================================================
  // LAZY EXPIRY
  // =====================================================

  // 1. Lazy expiry с исключением для recurring

  @Test
  void shouldExpireNonRecurringObligation() {

    Obligation expired = TestDataFactory.expiredNonRecurring();

    when(obligationRepository.findAll()).thenReturn(List.of(expired));

    service.getObligations(null, null);

    assertEquals(Status.EXPIRED, expired.getStatus());
  }

  @Test
  void shouldNotExpireRecurringObligation() {

    Obligation recurring = TestDataFactory.expiredRecurring();

    when(obligationRepository.findAll()).thenReturn(List.of(recurring));

    service.getObligations(null, null);

    assertEquals(Status.ACTIVE, recurring.getStatus());
  }

  @Test
  void shouldLoadByCategoryAndStatus() {

    when(obligationRepository.findByCategoryAndStatus(Category.SUBSCRIPTION, Status.ACTIVE))
        .thenReturn(List.of(obligation));

    List<ObligationDto> result = service.getObligations(Category.SUBSCRIPTION, Status.ACTIVE);

    assertEquals(1, result.size());

    verify(obligationRepository).findByCategoryAndStatus(Category.SUBSCRIPTION, Status.ACTIVE);
  }

  @Test
  void shouldLoadByCategoryOnly() {

    when(obligationRepository.findByCategory(Category.SUBSCRIPTION))
        .thenReturn(List.of(obligation));

    List<ObligationDto> result = service.getObligations(Category.SUBSCRIPTION, null);

    assertEquals(1, result.size());

    verify(obligationRepository).findByCategory(Category.SUBSCRIPTION);

    verify(obligationRepository, never()).findByStatus(any());

    verify(obligationRepository, never()).findAll();
  }

  @Test
  void shouldLoadByStatusOnly() {

    when(obligationRepository.findByStatus(Status.ACTIVE)).thenReturn(List.of(obligation));

    List<ObligationDto> result = service.getObligations(null, Status.ACTIVE);

    assertEquals(1, result.size());

    verify(obligationRepository).findByStatus(Status.ACTIVE);

    verify(obligationRepository, never()).findByCategory(any());

    verify(obligationRepository, never()).findAll();
  }

  @Test
  void shouldSortObligationsByNextPaymentDate() {

    Obligation first =
        Obligation.create(
            "First",
            BigDecimal.valueOf(100),
            "USD",
            Category.BILL,
            null,
            LocalDate.now().plusDays(10));

    Obligation second =
        Obligation.create(
            "Second",
            BigDecimal.valueOf(200),
            "USD",
            Category.BILL,
            null,
            LocalDate.now().plusDays(2));

    when(obligationRepository.findAll()).thenReturn(List.of(first, second));

    List<ObligationDto> result = service.getObligations(null, null);

    assertEquals("Second", result.get(0).title());
  }

  // =====================================================
  // PAY
  // =====================================================

  // 2. /pay для каждого recurrence

  @Test
  void shouldRescheduleMonthlyAfterPayment() {

    Obligation monthly = TestDataFactory.activeMonthly();

    LocalDate oldDate = monthly.getNextPaymentDate();

    when(obligationRepository.findById(monthly.getId())).thenReturn(java.util.Optional.of(monthly));

    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.pay(monthly.getId(), BigDecimal.valueOf(500));

    assertEquals(oldDate.plusMonths(1), monthly.getNextPaymentDate());

    assertEquals(Status.ACTIVE, monthly.getStatus());
  }

  @Test
  void shouldRescheduleQuarterlyAfterPayment() {

    Obligation quarterly = TestDataFactory.activeQuarterly();

    LocalDate oldDate = quarterly.getNextPaymentDate();

    when(obligationRepository.findById(quarterly.getId()))
        .thenReturn(java.util.Optional.of(quarterly));

    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.pay(quarterly.getId(), BigDecimal.valueOf(1000));

    assertEquals(oldDate.plusMonths(3), quarterly.getNextPaymentDate());
  }

  @Test
  void shouldRescheduleYearlyAfterPayment() {

    Obligation yearly = TestDataFactory.activeYearly();

    LocalDate oldDate = yearly.getNextPaymentDate();

    when(obligationRepository.findById(yearly.getId())).thenReturn(java.util.Optional.of(yearly));

    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.pay(yearly.getId(), BigDecimal.valueOf(2000));

    assertEquals(oldDate.plusYears(1), yearly.getNextPaymentDate());
  }

  @Test
  void shouldCancelAfterPaymentWithoutRecurrence() {

    Obligation obligation = TestDataFactory.activeWithoutRecurrence();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.pay(obligation.getId(), BigDecimal.valueOf(300));

    assertEquals(Status.CANCELLED, obligation.getStatus());
  }

  // 3. Граничный случай 31 число + monthly

  @Test
  void shouldHandleJanuary31MonthlyPayment() {

    Obligation obligation = TestDataFactory.january31Monthly();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.pay(obligation.getId(), BigDecimal.valueOf(500));

    assertEquals(LocalDate.of(2026, 2, 28), obligation.getNextPaymentDate());
  }

  // 4. Попытка оплатить или отменить не-active обязательство

  @Test
  void shouldThrowWhenPayingInactiveObligation() {

    Obligation cancelled = TestDataFactory.activeMonthly();

    cancelled.cancel();

    when(obligationRepository.findById(cancelled.getId()))
        .thenReturn(java.util.Optional.of(cancelled));

    assertThrows(
        RuntimeException.class, () -> service.pay(cancelled.getId(), BigDecimal.valueOf(500)));
  }

  // =====================================================
  // CANCEL
  // =====================================================

  @Test
  void shouldCancelActiveObligation() {

    Obligation obligation = TestDataFactory.activeMonthly();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    when(obligationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Obligation result = service.cancel(obligation.getId());

    assertEquals(Status.CANCELLED, result.getStatus());
  }

  @Test
  void shouldThrowWhenCancellingExpiredObligation() {

    Obligation obligation = TestDataFactory.expiredNonRecurring();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    assertThrows(RuntimeException.class, () -> service.cancel(obligation.getId()));

    assertEquals(Status.EXPIRED, obligation.getStatus());
  }

  @Test
  void shouldThrowWhenCancellingCancelledObligation() {

    Obligation obligation = TestDataFactory.activeWithoutRecurrence();

    obligation.cancel();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    assertEquals(Status.CANCELLED, obligation.getStatus());

    assertThrows(RuntimeException.class, () -> service.cancel(obligation.getId()));
  }

  // =====================================================
  // DELETE + SSE
  // =====================================================

  @Test
  void shouldDeleteObligationAndSendSseEvent() {

    Obligation obligation = TestDataFactory.activeMonthly();

    when(obligationRepository.findById(obligation.getId()))
        .thenReturn(java.util.Optional.of(obligation));

    service.delete(obligation.getId());

    verify(obligationRepository).delete(obligation);

    verify(sseService).send(eq("obligation_deleted"), any());
  }

  // =====================================================
  // UPCOMING
  // =====================================================

  @Test
  void shouldCalculateUpcomingTotalsAndAlerts() {

    Obligation monthly = TestDataFactory.activeMonthly();

    Obligation bill = TestDataFactory.activeWithoutRecurrence();

    when(obligationRepository.findByNextPaymentDateBetween(
            any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(monthly, bill));

    var result = service.getUpcoming(30);

    assertEquals(BigDecimal.valueOf(800), result.totals().get("USD"));

    assertEquals(1, result.renewalAlerts().size());

    assertEquals(2, result.obligations().size());
  }
}

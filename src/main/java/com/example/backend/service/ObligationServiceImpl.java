package com.example.backend.service;

import com.example.backend.dto.CreateObligationResponse;
import com.example.backend.dto.ObligationCreateRequest;
import com.example.backend.dto.ObligationDeletedEvent;
import com.example.backend.dto.ObligationDto;
import com.example.backend.dto.PayObligationResponse;
import com.example.backend.dto.PaymentDto;
import com.example.backend.dto.RenewalAlertDto;
import com.example.backend.dto.UpcomingObligationsResponse;
import com.example.backend.entity.Obligation;
import com.example.backend.entity.Payment;
import com.example.backend.enums.Category;
import com.example.backend.enums.Status;
import com.example.backend.exception_handling.ResourceNotFoundException;
import com.example.backend.exception_handling.UnprocessableContentException;
import com.example.backend.repository.ObligationRepository;
import com.example.backend.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObligationServiceImpl implements ObligationService {

  private static final String DUPLICATE_WARNING =
      "Активное обязательство с таким названием уже существует";

  private final ObligationRepository obligationRepository;
  private final PaymentRepository paymentRepository;
  private final SseService sseService;

  @Override
  @Transactional
  public CreateObligationResponse createObligation(ObligationCreateRequest request) {

    boolean duplicateExists =
        obligationRepository.existsByStatusAndTitleIgnoreCase(Status.ACTIVE, request.title());

    Obligation obligation =
        Obligation.create(
            request.title(),
            request.amount(),
            request.currency(),
            request.category(),
            request.recurrence(),
            request.nextPaymentDate());

    if (request.nextPaymentDate().isBefore(LocalDate.now())) {
      obligation.expire();
    }

    Obligation saved = obligationRepository.save(obligation);

    return new CreateObligationResponse(
        ObligationDto.from(saved), duplicateExists ? DUPLICATE_WARNING : null);
  }

  @Override
  @Transactional
  public List<ObligationDto> getObligations(Category category, Status status) {

    List<Obligation> obligations = load(category, status);

    applyLazyExpiry(obligations);

    return obligations.stream()
        .sorted(Comparator.comparing(Obligation::getNextPaymentDate))
        .map(ObligationDto::from)
        .toList();
  }

  private List<Obligation> load(Category category, Status status) {

    if (category != null && status != null) {
      return obligationRepository.findByCategoryAndStatus(category, status);
    }

    if (category != null) {
      return obligationRepository.findByCategory(category);
    }

    if (status != null) {
      return obligationRepository.findByStatus(status);
    }

    return obligationRepository.findAll();
  }

  private void applyLazyExpiry(List<Obligation> obligations) {

    LocalDate today = LocalDate.now();

    for (Obligation o : obligations) {

      if (o.getStatus() == Status.ACTIVE
          && o.getRecurrence() == null
          && o.getNextPaymentDate().isBefore(today)) {

        o.expire(); // domain method
      }
    }
  }

  @Transactional(readOnly = true)
  public UpcomingObligationsResponse getUpcoming(int days) {

    // 🔹 Определяем временное окно для выборки
    // Это application-level concern (use case input processing)
    LocalDate today = LocalDate.now();
    LocalDate end = today.plusDays(days);

    // 🔹 Получаем данные из репозитория (DB layer)
    // Здесь уже применяется фильтрация на уровне базы данных
    List<Obligation> filtered = obligationRepository.findByNextPaymentDateBetween(today, end);

    // 🔹 Read model агрегаты (проекции для ответа API)
    Map<String, BigDecimal> totals = new HashMap<>();
    List<RenewalAlertDto> alerts = new ArrayList<>();
    List<ObligationDto> obligations = new ArrayList<>();

    // 🔹 Один проход по данным — оптимизированная сборка response model
    // Это классический "manual projection builder"
    for (Obligation o : filtered) {

      // 📌 Full DTO representation (для UI списка)
      obligations.add(ObligationDto.from(o));

      // 📌 Aggregation layer:
      // считаем суммы по валютам (no domain logic, pure projection)
      totals.merge(o.getCurrency(), o.getAmount(), BigDecimal::add);

      // 📌 Business-facing subset:
      // renewal alerts — ключевая продуктовая фича
      // показываем только recurring обязательства (подписки)
      if (o.getCategory() == Category.SUBSCRIPTION && o.getRecurrence() != null) {

        alerts.add(
            new RenewalAlertDto(
                o.getId(), o.getTitle(), o.getNextPaymentDate(), o.getAmount(), o.getCurrency()));
      }
    }

    // 🔹 Сборка response DTO (read model)
    return new UpcomingObligationsResponse(obligations, totals, alerts);
  }

  @Transactional
  public PayObligationResponse pay(UUID id, BigDecimal amount) {

    Obligation obligation =
        obligationRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Obligation with id %s not found".formatted(id)));

    // Правило 1: статус должен быть ACTIVE
    if (obligation.getStatus() != Status.ACTIVE) {
      throw new UnprocessableContentException("Only active obligations can be paid");
    }

    // 1. создаём payment через aggregate
    Payment payment = obligation.pay(amount);

    // 2. сохраняем payment
    Payment savedPayment = paymentRepository.save(payment);

    // 3. применяем бизнес-правило по recurrence
    applyRecurrenceRule(obligation);

    // 4. сохраняем обновлённое obligation
    Obligation savedObligation = obligationRepository.save(obligation);

    return new PayObligationResponse(
        ObligationDto.from(savedObligation), PaymentDto.from(savedPayment));
  }

  private void applyRecurrenceRule(Obligation o) {

    LocalDate current = o.getNextPaymentDate();

    if (o.getRecurrence() != null) {

      switch (o.getRecurrence()) {
        case MONTHLY -> o.reschedule(addMonthsSafe(current, 1));

        case QUARTERLY -> o.reschedule(addMonthsSafe(current, 3));

        case YEARLY -> o.reschedule(addYearsSafe(current, 1));
      }

      o.activate();

    } else {

      o.cancel();
    }
  }

  private LocalDate addMonthsSafe(LocalDate date, int months) {
    return date.plusMonths(months);
  }

  private LocalDate addYearsSafe(LocalDate date, int years) {
    return date.plusYears(years);
  }

  @Transactional
  public Obligation cancel(UUID id) {

    Obligation obligation =
        obligationRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Obligation with id %s not found".formatted(id)));

    obligation.cancel();

    return obligationRepository.save(obligation);
  }

  @Transactional
  public void delete(UUID id) {

    Obligation obligation =
        obligationRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Obligation with id %s not found".formatted(id)));

    obligationRepository.delete(obligation);

    sseService.send("obligation_deleted", new ObligationDeletedEvent(id));
  }
}

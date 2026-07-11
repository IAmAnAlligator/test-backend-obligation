package com.example.backend.service;

import com.example.backend.dto.CreateObligationResponse;
import com.example.backend.dto.ObligationCreateRequest;
import com.example.backend.dto.ObligationDto;
import com.example.backend.dto.PayObligationResponse;
import com.example.backend.dto.UpcomingObligationsResponse;
import com.example.backend.entity.Obligation;
import com.example.backend.enums.Category;
import com.example.backend.enums.Status;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ObligationService {

  CreateObligationResponse createObligation(ObligationCreateRequest request);

  List<ObligationDto> getObligations(Category category, Status status);

  UpcomingObligationsResponse getUpcoming(int days);

  PayObligationResponse pay(UUID id, BigDecimal amount);

  Obligation cancel(UUID id);

  void delete(UUID id);
}

package com.example.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record UpcomingObligationsResponse(
    List<ObligationDto> obligations,
    Map<String, BigDecimal> totals,
    List<RenewalAlertDto> renewalAlerts) {}

package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record CreateObligationResponse(
    ObligationDto obligation, @JsonInclude(JsonInclude.Include.NON_NULL) String warning) {}

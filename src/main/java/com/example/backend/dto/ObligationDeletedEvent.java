package com.example.backend.dto;

import java.util.UUID;

public record ObligationDeletedEvent(String type, UUID id) {

  public ObligationDeletedEvent(UUID id) {
    this("obligation_deleted", id);
  }
}

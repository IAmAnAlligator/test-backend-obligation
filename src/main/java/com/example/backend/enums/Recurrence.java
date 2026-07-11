package com.example.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Recurrence {
  MONTHLY,
  QUARTERLY,
  YEARLY;

  @JsonCreator
  public static Recurrence from(String value) {
    return value == null ? null : Recurrence.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }
}

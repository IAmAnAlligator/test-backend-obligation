package com.example.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Category {
  SUBSCRIPTION,
  WARRANTY,
  BILL,
  INSURANCE;

  @JsonCreator
  public static Category from(String value) {
    return value == null ? null : Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }
}

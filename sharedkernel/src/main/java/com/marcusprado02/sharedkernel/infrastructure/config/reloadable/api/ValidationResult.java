package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

import java.util.List;
import java.util.Map;

public record ValidationResult(boolean valid, List<String> errors, Map<String,Object> meta) {
  public static ValidationResult ok() { return new ValidationResult(true, List.of(), Map.of()); }
  public static ValidationResult error(String msg) { return new ValidationResult(false, List.of(msg), Map.of()); }
  public String message() { return errors == null || errors.isEmpty() ? "" : String.join("; ", errors); }
}
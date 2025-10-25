package com.marcusprado02.sharedkernel.infrastructure.config.validation.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ValidationResult(boolean valid, List<String> errors, Map<String,Object> meta) {
  public static ValidationResult ok(){ return new ValidationResult(true, List.of(), Map.of()); }
  public static ValidationResult error(String msg){ return new ValidationResult(false, List.of(msg), Map.of()); }
  public ValidationResult merge(ValidationResult other){
    return new ValidationResult(this.valid && other.valid,
      Stream.concat(this.errors.stream(), other.errors.stream()).toList(),
      Stream.concat(this.meta.entrySet().stream(), other.meta.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->b)));
  }
  public String message(){ return errors == null || errors.isEmpty() ? "" : String.join("; ", errors); }
}
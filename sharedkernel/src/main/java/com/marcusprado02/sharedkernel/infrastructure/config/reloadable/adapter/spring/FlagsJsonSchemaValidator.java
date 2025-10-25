package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;

import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ConfigValidator;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ValidationResult;

/** Stub leve (pode integrar um validador de JSON Schema depois). */
public final class FlagsJsonSchemaValidator implements ConfigValidator<Map<String, FlagDefinition>> {
  private final String schemaPath;
  public FlagsJsonSchemaValidator(String schemaPath) { this.schemaPath = schemaPath; }
  @Override public ValidationResult validate(Map<String, FlagDefinition> candidate) {
    // Aqui você integraria um motor de JSON Schema. Por ora, sempre OK.
    return ValidationResult.ok();
  }
}

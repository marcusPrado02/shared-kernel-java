package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

import java.util.List;
import java.util.Map;

public record FlagDefinition(
  String key,
  boolean enabled,
  List<Prerequisite> prerequisites,
  List<Rule> rules,             // targeting (conditions + rollout)
  Variant defaultVariant,       // usado em fallthrough
  Map<String, Variant> variants,// nome→valor tipado
  String description,
  long version                  // p/ cache busting e auditoria
) {}
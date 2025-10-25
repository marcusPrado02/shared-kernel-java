package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping;

import java.util.Map;

public record EdgeCommand(
  String edgeLabel,
  Object fromId,
  Object toId,
  Map<String,Object> properties // ex: weight, since, etc.
) {}

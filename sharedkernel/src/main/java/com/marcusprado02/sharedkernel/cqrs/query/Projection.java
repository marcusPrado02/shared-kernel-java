package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.Map;

/** Projeção/shape: define campos/joins permitidos p/ DTOs. */
public record Projection(String name, Map<String, ?> hints) { }

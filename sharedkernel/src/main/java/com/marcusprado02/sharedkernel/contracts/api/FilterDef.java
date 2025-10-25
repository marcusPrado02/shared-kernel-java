package com.marcusprado02.sharedkernel.contracts.api;

import java.util.function.Predicate;

/** Metadados para ordering e aplicação condicional. */
public record FilterDef(String name, ApiFilter filter, int order, Predicate<ApiExchange> when) {}


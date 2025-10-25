package com.marcusprado02.sharedkernel.contracts.api;

public sealed interface FilterResult permits FilterResult.Proceed, FilterResult.Halt {
    record Proceed(ApiExchange exchange) implements FilterResult {}
    record Halt(ApiExchange exchange) implements FilterResult {}
}

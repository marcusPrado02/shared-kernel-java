package com.marcusprado02.sharedkernel.domain.service.error;

public record ConcurrencyException(String code, String message) implements DomainException {}

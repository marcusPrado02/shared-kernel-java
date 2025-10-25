package com.marcusprado02.sharedkernel.domain.service.error;

public record BusinessRuleException(String code, String message) implements DomainException {}

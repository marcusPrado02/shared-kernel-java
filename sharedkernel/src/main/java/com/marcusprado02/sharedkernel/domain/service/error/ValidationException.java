package com.marcusprado02.sharedkernel.domain.service.error;

public record ValidationException(String code, String message) implements DomainException {}
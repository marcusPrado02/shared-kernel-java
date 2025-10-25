package com.marcusprado02.sharedkernel.domain.repository.errors;

public final class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}

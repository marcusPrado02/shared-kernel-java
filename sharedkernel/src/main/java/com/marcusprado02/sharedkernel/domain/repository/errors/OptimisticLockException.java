package com.marcusprado02.sharedkernel.domain.repository.errors;


/** Mapear do adapter (JPA/SQL) para o domínio ao detectar conflito de versão */
public final class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String message, Throwable cause) { super(message, cause); }
}

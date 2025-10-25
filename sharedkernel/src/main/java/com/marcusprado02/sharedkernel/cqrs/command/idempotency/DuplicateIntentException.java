package com.marcusprado02.sharedkernel.cqrs.command.idempotency;

/** Lançada quando a mesma intenção (idempotency key) já foi reivindicada. */
public class DuplicateIntentException extends RuntimeException {
  private final String code = "IDEMPOTENCY_DUPLICATE";
  private final boolean retryable = false;

  public DuplicateIntentException() { super("Duplicate intent"); }
  public DuplicateIntentException(String msg) { super(msg); }

  /** Código canônico para mapeamento de erro (ex.: 409 Conflict). */
  public String code() { return code; }
  /** Sinaliza ao mapper que não é para retry automático. */
  public boolean retryable() { return retryable; }
}

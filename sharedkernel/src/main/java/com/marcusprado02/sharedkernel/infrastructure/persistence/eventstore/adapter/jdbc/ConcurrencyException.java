package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.jdbc;

public class ConcurrencyException extends RuntimeException {
  public ConcurrencyException(String m) { super(m); }
}

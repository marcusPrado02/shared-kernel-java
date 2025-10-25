package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Posição global (offset) do log de eventos.
 * Inteiro sem sinal representado em long.
 */
public record GlobalPosition(long value) implements Comparable<GlobalPosition> {

  /** Início do log. */
  public static final GlobalPosition START = new GlobalPosition(0L);

  /** Fábrica conveniente. */
  public static GlobalPosition of(long v) { return new GlobalPosition(v); }

  /** Próxima posição (simples). */
  public GlobalPosition next() { return new GlobalPosition(value + 1); }

  @Override public int compareTo(GlobalPosition o) { return Long.compareUnsigned(this.value, o.value); }

  /** Serialização JSON: usa string sem sinal para evitar problemas de signed long. */
  @JsonValue public String asString() { return Long.toUnsignedString(value); }

  /** Desserialização JSON. */
  @JsonCreator public static GlobalPosition from(String s) {
    return new GlobalPosition(Long.parseUnsignedLong(s));
  }

  @Override public String toString() { return asString(); }
}

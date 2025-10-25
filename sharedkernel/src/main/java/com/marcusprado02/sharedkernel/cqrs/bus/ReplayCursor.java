package com.marcusprado02.sharedkernel.cqrs.bus;

/**
 * Cursor para reler eventos: por stream específico ou em "all-streams".
 * offset: posição/seq numérica no log (semântica definida pelo seu EventStore).
 */
public record ReplayCursor(String stream, long offset) {
  /** Cursor para todos os streams a partir de um offset. */
  public static ReplayCursor all(long offset) { return new ReplayCursor(null, offset); }

  /** Verdadeiro se for cursor de todos os streams. */
  public boolean allStreams() { return stream == null || stream.isBlank(); }
}

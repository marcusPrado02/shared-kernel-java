package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;


/** Opções para leitura de streams. */
public record ReadOptions(
    long fromRevisionInclusive,
    int maxCount,
    ReadDirection direction,
    boolean resolveLinks,               // útil em EventStoreDB
    boolean includeMetadataOnly         // útil para projeções leves
) {
  public static ReadOptions fromStart(int max) { return new ReadOptions(0, max, ReadDirection.FORWARD, false, false); }
}

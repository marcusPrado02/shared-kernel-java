package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** ULID simples (troque por lib de sua preferência). */
final class Ulids {
  public static String nextUlid() { return java.util.UUID.randomUUID().toString().replace("-", ""); }
}

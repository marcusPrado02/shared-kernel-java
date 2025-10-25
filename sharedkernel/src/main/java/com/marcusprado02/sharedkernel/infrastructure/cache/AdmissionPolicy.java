package com.marcusprado02.sharedkernel.infrastructure.cache;

/** Política de admissão (TinyLFU/CountMin) – decide se entra no cache. */
public interface AdmissionPolicy<V> {
  boolean admit(String key, V value, long weight);
}

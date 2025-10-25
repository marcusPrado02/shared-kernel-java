package com.marcusprado02.sharedkernel.crosscutting.cache;

/** Armazenamento de cache simples para Decorators/Ports. */
public interface CacheStore {
    /** @return valor armazenado ou null se ausente/expirado */
    Object get(String key);

    /** Define um valor no cache com TTL em segundos. TTL <= 0 implica "não armazenar". */
    void set(String key, Object value, long ttlSeconds);
}
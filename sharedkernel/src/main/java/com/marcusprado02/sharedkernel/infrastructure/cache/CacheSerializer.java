package com.marcusprado02.sharedkernel.infrastructure.cache;

/** Serialização plugável (JSON/Smile/Avro/Proto + compressão). */
public interface CacheSerializer<V> {
  byte[] serialize(V value, boolean compress);
  V deserialize(byte[] bytes, boolean compressed, Class<V> type);
  String contentType();
}

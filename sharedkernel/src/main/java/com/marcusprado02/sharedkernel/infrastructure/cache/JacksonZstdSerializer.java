package com.marcusprado02.sharedkernel.infrastructure.cache;

public class JacksonZstdSerializer<V> implements CacheSerializer<V> {
  private final com.fasterxml.jackson.databind.ObjectMapper om;
  private final Class<V> type;
  public JacksonZstdSerializer(Class<V> t) { this.om = new com.fasterxml.jackson.databind.ObjectMapper(); this.type = t; }

  @Override public byte[] serialize(V value, boolean compress) {
    try {
      byte[] json = om.writeValueAsBytes(value);
      if (!compress) return json;
      return com.github.luben.zstd.Zstd.compress(json);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  @Override public V deserialize(byte[] bytes, boolean compressed, Class<V> type) {
    try {
      byte[] data = compressed ? com.github.luben.zstd.Zstd.decompress(bytes, (int)com.github.luben.zstd.Zstd.decompressedSize(bytes)) : bytes;
      return om.readValue(data, type);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  @Override public String contentType() { return "application/json+zstd"; }
}


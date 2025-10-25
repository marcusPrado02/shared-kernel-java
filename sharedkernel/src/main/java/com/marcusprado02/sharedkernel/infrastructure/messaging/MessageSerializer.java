package com.marcusprado02.sharedkernel.infrastructure.messaging;

/** Serializador pluggável (JSON/Avro/Proto/CloudEvents). */
public interface MessageSerializer {
  <T> byte[] serialize(MessageEnvelope<T> envelope);
  <T> MessageEnvelope<T> deserialize(byte[] bytes, Class<T> payloadType);
  String contentType();
}

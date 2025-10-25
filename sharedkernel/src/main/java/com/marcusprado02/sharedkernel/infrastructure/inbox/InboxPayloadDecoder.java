package com.marcusprado02.sharedkernel.infrastructure.inbox;

import java.util.Map;

/** Validação e mapeamento do payload. */
public interface InboxPayloadDecoder {
  <T> T decode(String contentType, String json, Class<T> type, Map<String,String> headers);
  /** opcional: validação por Avro/JSON-Schema */
  void validate(String contentType, String json, String schemaRefOrVersion);
}
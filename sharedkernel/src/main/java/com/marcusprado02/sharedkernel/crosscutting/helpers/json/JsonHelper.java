package com.marcusprado02.sharedkernel.crosscutting.helpers.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonHelper {
  private static final ObjectMapper M = new ObjectMapper()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .registerModule(new JavaTimeModule());

  private JsonHelper(){}
  public static String write(Object o) { try { return M.writeValueAsString(o); }
    catch (Exception e) { throw new IllegalArgumentException("json write", e); } }
  public static <T> T read(String json, Class<T> type) {
    try { return M.readValue(json, type); }
    catch (Exception e) { throw new IllegalArgumentException("json read", e); }
  }
}

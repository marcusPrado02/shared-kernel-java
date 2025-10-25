package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Evite em produção; substitua por Jackson/Avro etc. */
public class Jsons {
  static String toJson(Object o) { try { return new ObjectMapper().writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); } }
  static <T> T fromJson(String j, Class<T> t) { try { return new ObjectMapper().readValue(j, t); } catch (Exception e) { throw new RuntimeException(e); } }
}
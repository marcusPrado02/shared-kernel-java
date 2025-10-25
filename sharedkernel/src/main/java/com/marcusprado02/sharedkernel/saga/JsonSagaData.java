package com.marcusprado02.sharedkernel.saga;


import java.util.Map;
import java.util.Objects;

/** Implementação simples de SagaData baseada em Map para casos "raw". */
public final class JsonSagaData implements SagaData {
  private final Map<String,Object> value;

  public JsonSagaData(Map<String,Object> value) {
    this.value = (value == null ? Map.of() : Map.copyOf(value));
  }

  public Map<String,Object> asMap() { return value; }

  @Override public String toString() { return "JsonSagaData" + value; }
  @Override public int hashCode()    { return Objects.hash(value); }
  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JsonSagaData other)) return false;
    return Objects.equals(value, other.value);
  }
}
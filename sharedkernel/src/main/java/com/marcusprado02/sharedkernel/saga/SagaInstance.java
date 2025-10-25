package com.marcusprado02.sharedkernel.saga;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

public final class SagaInstance<D extends SagaData> {
    public String sagaId;
    public String sagaName;
    public SagaStatus status;
    public String currentStep;
    public D data;
    public int version; // lock otimista
    public Instant updatedAt;

    public SagaInstance(String sagaId, String sagaName, SagaStatus status, String currentStep, D data, int version, Instant updatedAt) {
        this.sagaId = sagaId;
        this.sagaName = sagaName;
        this.status = status;
        this.currentStep = currentStep;
        this.data = data;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    public String sagaId() { return sagaId; }
    public String sagaName() { return sagaName; }
    public SagaStatus status() { return status; }
    public String currentStep() { return currentStep; } 
    public D data() { return data; }
    public int version() { return version; }
    public Instant updatedAt() { return updatedAt; }

     /** Fábrica forte (D conhecido). Converte OffsetDateTime para Instant. */
  public static <D extends SagaData> SagaInstance<D> of(
      String sagaId,
      String sagaName,
      SagaStatus status,
      String currentStep,
      D data,
      Integer version,
      OffsetDateTime updatedAt
  ) {
    int v = (version != null ? version : 0);
    Instant ua = (updatedAt != null ? updatedAt.toInstant() : Instant.now());
    return new SagaInstance<>(sagaId, sagaName, status, currentStep, data, v, ua);
  }

  /** Fábrica "raw" para quando D é desconhecido (usa JsonSagaData para cumprir o bound). */
  public static SagaInstance<JsonSagaData> ofRaw(
      String sagaId,
      String sagaName,
      SagaStatus status,
      String currentStep,
      Map<String, Object> data,
      Integer version,
      OffsetDateTime updatedAt
  ) {
    return of(sagaId, sagaName, status, currentStep, new JsonSagaData(data), version, updatedAt);
  }
}

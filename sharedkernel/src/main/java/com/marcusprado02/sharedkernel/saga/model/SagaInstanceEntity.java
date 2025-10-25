package com.marcusprado02.sharedkernel.saga.model;

import java.time.OffsetDateTime;

import com.marcusprado02.sharedkernel.saga.SagaStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity @Table(name="saga_instances")
public class SagaInstanceEntity {
  @Id public String sagaId;
  public String sagaName;
  @Enumerated(EnumType.STRING) public SagaStatus status;
  public String currentStep;
  @Lob public String dataJson;
  public Integer version;
  public OffsetDateTime updatedAt;
}

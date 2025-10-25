package com.marcusprado02.sharedkernel.infrastructure.persistence.document.example.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;

import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxEvent;
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxPublisher;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

@Component
class MongoOutboxPublisher implements OutboxPublisher {
  private final MongoTemplate mongo;
  private final TenantProvider tenant;
  MongoOutboxPublisher(MongoTemplate mongo, TenantProvider tenant) {
    this.mongo = mongo; this.tenant = tenant;
  }
  @Override
  public void publish(List<? extends DomainEvent> events) {
    var docs = new ArrayList<OutboxEvent>();
    for (DomainEvent ev : events) {
      docs.add(new OutboxEvent(UUID.randomUUID().toString(), tenant.tenantId(),
          ev.getClass().getSimpleName(), extractAggId(ev), ev.getClass().getName(),
          Instant.now(), serialize(ev)));
    }
    mongo.insert(docs, "outbox");
  }
  private String extractAggId(DomainEvent ev){ /* implemente */ return "agg-id"; }
  private String serialize(DomainEvent ev){ /* Jackson */ return "{}"; }
}

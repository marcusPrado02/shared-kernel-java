package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.DomainEvent;
import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.EventEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.EventSerializer;

public class CloudEventsJsonSerializer implements EventSerializer {

  private final ObjectMapper om;

  public CloudEventsJsonSerializer() {
    this.om = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(JsonFormat.getCloudEventJacksonModule());
    // Evita timestamps numéricos para datas
    this.om.findAndRegisterModules();
    this.om.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public byte[] serialize(EventEnvelope<?> e) {
    try {
      byte[] envelopeBytes = om.writeValueAsBytes(e);

      CloudEvent ce = CloudEventBuilder.v1()
          .withId(e.eventId() != null ? e.eventId() : UUID.randomUUID().toString())
          .withType(e.eventType() + ";v=" + e.eventVersion())
          .withSource(URI.create("urn:stream:" + e.streamId()))
          .withSubject(e.streamId())
          .withTime(OffsetDateTime.ofInstant(e.occurredAt(), java.time.ZoneOffset.UTC))
          .withDataContentType("application/json")
          .withData(envelopeBytes) // gera "data_base64" conforme a spec
          .build();

      return om.writeValueAsBytes(ce);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to serialize CloudEvent", ex);
    }
  }

  @Override
  public <E extends DomainEvent> EventEnvelope<E> deserialize(byte[] bytes, Class<E> payloadType) {
    try {
      // 1) Lê o CloudEvent
      CloudEvent ce = om.readValue(bytes, CloudEvent.class);

      if (ce == null || ce.getData() == null) {
        throw new IllegalStateException("CloudEvent has no data payload");
      }

      byte[] data = ce.getData().toBytes(); // data_base64 -> bytes

      // 2) Constrói tipo paramétrico EventEnvelope<E>
      var type = om.getTypeFactory()
          .constructParametricType(EventEnvelope.class, payloadType);

      // 3) Desserializa o envelope original a partir do 'data'
      return om.readValue(data, type);

    } catch (Exception ex) {
      throw new RuntimeException("Failed to deserialize CloudEvent payload to EventEnvelope<" +
          payloadType.getSimpleName() + ">", ex);
    }
  }

  @Override
  public String contentType() {
    return "application/cloudevents+json";
  }
}

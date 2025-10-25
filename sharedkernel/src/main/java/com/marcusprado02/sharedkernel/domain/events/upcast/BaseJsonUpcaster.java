package com.marcusprado02.sharedkernel.domain.events.upcast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marcusprado02.sharedkernel.domain.events.model.Content;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;
import com.marcusprado02.sharedkernel.domain.events.model.EventMetadata;

public abstract class BaseJsonUpcaster implements EventUpcaster {
    protected final ObjectMapper mapper = new ObjectMapper();

    protected EventEnvelope withJson(EventEnvelope in, ObjectNode newJson, int newVersion) {
        byte[] bytes;
        try { bytes = mapper.writeValueAsBytes(newJson); } catch (Exception e){ throw new RuntimeException(e); }
        var meta = new EventMetadata(
                in.metadata().eventId(),
                in.metadata().eventType(),
                newVersion,
                in.metadata().aggregateType(),
                in.metadata().aggregateId(),
                in.metadata().sequence(),
                in.metadata().aggregateVersion(),
                in.metadata().tenantId(),
                in.metadata().correlationId(),
                in.metadata().causationId(),
                in.metadata().occurredAt(),
                in.metadata().tags()
        );
        return in.with(meta, Content.json(bytes, in.payload().schemaId()));
    }

    protected ObjectNode readObjectNode(EventEnvelope in) {
        try { return (ObjectNode) mapper.readTree(in.payload().data()); } catch (Exception e){ throw new RuntimeException(e); }
    }
}

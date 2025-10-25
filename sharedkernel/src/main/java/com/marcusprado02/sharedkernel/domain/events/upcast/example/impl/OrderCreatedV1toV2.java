package com.marcusprado02.sharedkernel.domain.events.upcast.example.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;
import com.marcusprado02.sharedkernel.domain.events.model.EventType;
import com.marcusprado02.sharedkernel.domain.events.upcast.BaseJsonUpcaster;
import com.marcusprado02.sharedkernel.domain.events.upcast.UpcastContext;
import com.marcusprado02.sharedkernel.domain.events.upcast.UpcastResult;

public final class OrderCreatedV1toV2 extends BaseJsonUpcaster {
    private static final EventType TYPE = new EventType("order", "OrderCreated");

    @Override public EventType eventType() { return TYPE; }
    @Override public int fromVersion() { return 1; }
    @Override public int toVersion() { return 2; }

    @Override
    public UpcastResult apply(EventEnvelope in, UpcastContext ctx) {
        try {
            ObjectNode json = readObjectNode(in);
            if (json.has("total")) {
                json.set("amount", json.get("total"));
                json.remove("total");
            }
            json.putIfAbsent("customerId", JsonNodeFactory.instance.textNode("unknown"));
            var out = withJson(in, json, 2);
            return new UpcastResult.Changed(out, "rename_total_to_amount_add_customerId");
        } catch (Exception e){
            return new UpcastResult.Failed(in, "v1_to_v2_error", e);
        }
    }
}

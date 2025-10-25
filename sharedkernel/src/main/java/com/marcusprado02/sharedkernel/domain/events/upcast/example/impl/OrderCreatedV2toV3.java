package com.marcusprado02.sharedkernel.domain.events.upcast.example.impl;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;
import com.marcusprado02.sharedkernel.domain.events.model.EventType;
import com.marcusprado02.sharedkernel.domain.events.upcast.BaseJsonUpcaster;
import com.marcusprado02.sharedkernel.domain.events.upcast.UpcastContext;
import com.marcusprado02.sharedkernel.domain.events.upcast.UpcastResult;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class OrderCreatedV2toV3 extends BaseJsonUpcaster {
    private static final EventType TYPE = new EventType("order", "OrderCreated");
    private static final JsonNodeFactory F = JsonNodeFactory.instance;

    @Override public EventType eventType() { return TYPE; }
    @Override public int fromVersion() { return 2; }
    @Override public int toVersion() { return 3; }

    @Override
    public UpcastResult apply(EventEnvelope in, UpcastContext ctx) {
        try {
            ObjectNode json = readObjectNode(in);
            var money = F.objectNode();
            money.set("amount", json.get("amount"));
            money.set("currency", json.get("currency"));
            json.set("money", money);
            json.remove("amount");
            json.remove("currency");
            var out = withJson(in, json, 3);
            return new UpcastResult.Changed(out, "amount_currency_to_money");
        } catch (Exception e){
            return new UpcastResult.Failed(in, "v2_to_v3_error", e);
        }
    }
}
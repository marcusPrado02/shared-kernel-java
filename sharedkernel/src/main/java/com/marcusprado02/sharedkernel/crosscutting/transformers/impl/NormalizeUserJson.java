package com.marcusprado02.sharedkernel.crosscutting.transformers.impl;


import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marcusprado02.sharedkernel.crosscutting.transformers.core.*;

public final class NormalizeUserJson implements TransformFunction<JsonNode, JsonNode> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public TransformResult<JsonNode> apply(JsonNode in, TransformContext ctx) {
        // garante mutabilidade
        ObjectNode obj = (in != null && in.isObject())
                ? ((ObjectNode) in).deepCopy()
                : MAPPER.createObjectNode();

        if (obj.hasNonNull("email")) {
            obj.put("email", obj.get("email").asText().trim().toLowerCase(Locale.ROOT));
        }
        if (obj.hasNonNull("name")) {
            obj.put("name", obj.get("name").asText().trim());
        }
        // redaction opcional (hash do token, sem vazar o original)
        if (obj.has("token")) {
            obj.put("token", "***" + Math.abs(obj.get("token").asText().hashCode()));
        }
        return TransformResult.ok(obj);
    }
}
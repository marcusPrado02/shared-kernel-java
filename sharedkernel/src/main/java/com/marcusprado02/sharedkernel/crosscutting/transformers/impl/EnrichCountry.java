package com.marcusprado02.sharedkernel.crosscutting.transformers.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.crosscutting.transformers.core.*;

public final class EnrichCountry implements TransformFunction<JsonNode, JsonNode> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GeoIpPort geo;
    public EnrichCountry(GeoIpPort geo){ this.geo = geo; }

    @Override
    public TransformResult<JsonNode> apply(JsonNode in, TransformContext ctx) {
        try {
            String ip = in.path("ip").asText(null);
            if (ip == null) return TransformResult.ok(in);

            var country = geo.countryIso(ip);
            if (country.isEmpty()) return TransformResult.retry("geoip-miss", 100);

            // Garante mutabilidade
            ObjectNode obj = (in != null && in.isObject())
                    ? ((ObjectNode) in).deepCopy()
                    : MAPPER.createObjectNode();

            obj.put("country", country.get());
            return TransformResult.ok(obj);

        } catch (Exception e) {
            return TransformResult.retry("geoip-error", 250);
        }
    }
}
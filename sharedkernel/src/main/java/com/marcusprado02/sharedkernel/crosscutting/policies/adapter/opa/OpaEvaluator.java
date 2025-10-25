package com.marcusprado02.sharedkernel.crosscutting.policies.adapter.opa;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.Http;
import com.marcusprado02.sharedkernel.crosscutting.helpers.json.JsonHelper;
import com.marcusprado02.sharedkernel.crosscutting.policies.core.*;

public final class OpaEvaluator implements PolicyEvaluator {
    private final HttpClient http;           // seu Port/Decorator
    private final String url;                // ex.: http://opa:8181/v1/data/acme/allow

    public OpaEvaluator(HttpClient http, String url) { this.http = http; this.url = url; }

        
    @Override
    public Decision evaluate(Subject sub, String action, Resource res, Environment env) throws Exception {
        var input = Map.of("subject", sub, "action", action, "resource", res, "env", env);

        // Monte seu request conforme seu client (ajuste se não for esse HttpRequest):
        var req = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonHelper.write(Map.of("input", input))))
                .build();

        var rsp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (rsp.statusCode() >= 500) throw new IllegalStateException("OPA unavailable");

        JsonNode node = (JsonNode) JsonHelper.read(rsp.body(), JsonNode.class);   // <-- cast explícito
        JsonNode result = node.at("/result");

        boolean allow = result.path("allow").asBoolean(false);
        String policyId = result.path("policyId").asText("unknown");
        String reason = result.path("reason").asText("");
        @SuppressWarnings("unchecked")
        Map<String,Object> obligations = JsonHelper.read(result.path("obligations").toString(), Map.class);

        return new Decision(allow ? Effect.ALLOW : Effect.DENY, policyId, reason, obligations, traceId());
    }

    private String traceId(){ return io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId(); }
}
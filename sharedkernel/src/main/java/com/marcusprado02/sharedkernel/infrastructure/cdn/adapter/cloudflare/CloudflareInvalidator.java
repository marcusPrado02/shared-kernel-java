package com.marcusprado02.sharedkernel.infrastructure.cdn.adapter.cloudflare;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.cdn.*;

public class CloudflareInvalidator extends BaseCDNInvalidator {

    private final HttpClient http;
    private final String apiToken;
    private final String zoneId;

    public CloudflareInvalidator(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb,
                                 String apiToken, String zoneId) {
        super(tracer, meter, retry, cb);
        this.http = HttpClient.newHttpClient();
        this.apiToken = apiToken;
        this.zoneId = zoneId;
    }

    @Override
    protected InvalidateResponse doInvalidate(InvalidateRequest req) {
        var urls = req.targets().stream()
                .filter(t -> t.dimension() == Dimension.PATH)
                .map(Target::value).collect(Collectors.toList());

        var tags = req.targets().stream()
                .filter(t -> t.dimension() == Dimension.SURROGATE_KEY)
                .map(Target::value).collect(Collectors.toList());

        boolean purgeAll = req.targets().stream().anyMatch(t -> t.dimension() == Dimension.ALL);

        Map<String,Object> payload = new HashMap<>();
        if (purgeAll) payload.put("purge_everything", true);
        if (!urls.isEmpty()) payload.put("files", urls);
        if (!tags.isEmpty()) payload.put("tags", tags);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudflare.com/client/v4/zones/" + zoneId + "/purge_cache"))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                .build();

        try {
            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new RuntimeException("Cloudflare purge failed: " + resp.statusCode() + " " + resp.body());
            }
            Map<String,Object> raw = Map.of("httpStatus", resp.statusCode(), "body", resp.body());
            return new InvalidateResponse("Cloudflare", req.distributionId(), req.idempotencyKey(), Instant.now(), "Submitted", req.targets(), raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(Map<String,Object> m){
        // simples/placeholder; troque por Jackson em produção
        StringBuilder sb = new StringBuilder("{");
        int i=0; for (var e : m.entrySet()) {
            if (i++>0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof Boolean b) sb.append(b);
            else if (v instanceof List<?> l) {
                sb.append("[");
                for (int j=0;j<l.size();j++) {
                    if (j>0) sb.append(",");
                    sb.append("\"").append(l.get(j)).append("\"");
                }
                sb.append("]");
            } else sb.append("\"").append(v).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}

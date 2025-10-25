package com.marcusprado02.sharedkernel.infrastructure.cdn.adapter.cloudfront;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.cdn.*;

public class CloudFrontInvalidator extends BaseCDNInvalidator {

    private final CloudFrontClient cf;

    public CloudFrontInvalidator(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb,
                                 CloudFrontClient cf) {
        super(tracer, meter, retry, cb);
        this.cf = cf;
    }

    @Override
    protected InvalidateResponse doInvalidate(InvalidateRequest req) {
        // CloudFront só invalida por PATH; para SURROGATE_KEY/ALL não há API nativa
        List<String> paths = req.targets().stream()
                .filter(t -> t.dimension() == Dimension.PATH || t.dimension() == Dimension.ALL)
                .map(t -> t.dimension() == Dimension.ALL ? "/*" : normalizePath(t.value()))
                .distinct()
                .collect(Collectors.toList());
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("CloudFront only supports PATH/ALL. Received: " + req.targets());
        }

        CreateInvalidationRequest cir = CreateInvalidationRequest.builder()
                .distributionId(req.distributionId())
                .invalidationBatch(InvalidationBatch.builder()
                        .callerReference(req.idempotencyKey())
                        .paths(Paths.builder().items(paths).quantity(paths.size()).build())
                        .build())
                .build();

        CreateInvalidationResponse resp = cf.createInvalidation(cir);

        if (req.waitForCompletion()) {
            waitUntilCompleted(req.distributionId(), resp.invalidation().id());
        }

        Map<String,Object> raw = new HashMap<>();
        raw.put("id", resp.invalidation().id());
        raw.put("status", resp.invalidation().status());
        raw.put("createTime", resp.invalidation().createTime());

        return new InvalidateResponse(
                "CloudFront",
                req.distributionId(),
                resp.invalidation().id(),
                Instant.now(),
                resp.invalidation().status(),
                req.targets(),
                raw
        );
    }

    private void waitUntilCompleted(String distId, String invalidationId) {
        for (int i=0;i<60;i++) { // ~5 min máx com 5s
            GetInvalidationResponse r = cf.getInvalidation(GetInvalidationRequest.builder()
                    .distributionId(distId).id(invalidationId).build());
            if ("Completed".equalsIgnoreCase(r.invalidation().status())) return;
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("CloudFront invalidation did not complete in time: " + invalidationId);
    }

    private static String normalizePath(String p){
        if (!p.startsWith("/")) return "/" + p;
        return p;
    }
}

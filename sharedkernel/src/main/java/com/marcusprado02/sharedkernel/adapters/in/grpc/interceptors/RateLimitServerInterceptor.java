package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import io.grpc.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitServerInterceptor implements ServerInterceptor {
    private static class Bucket { int tokens; long lastRefill; }
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacity; private final int refillPerSec;

    public RateLimitServerInterceptor(int capacity, int refillPerSec) {
        this.capacity = capacity; this.refillPerSec = refillPerSec;
    }

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
       ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String subject = AuthServerInterceptor.CTX_SUBJECT.get() != null ? AuthServerInterceptor.CTX_SUBJECT.get() : "anon";
        String key = subject + ":" + call.getMethodDescriptor().getFullMethodName();

        Bucket b = buckets.computeIfAbsent(key, k -> { var nb = new Bucket(); nb.tokens = capacity; nb.lastRefill = Instant.now().getEpochSecond(); return nb; });
        refill(b);

        if (b.tokens <= 0) {
            StatusRuntimeException ex = Status.RESOURCE_EXHAUSTED
                    .withDescription("Rate limit exceeded").asRuntimeException();
            call.close(ex.getStatus(), new Metadata());
            return new ServerCall.Listener<>() {};
        }
        b.tokens--;
        return next.startCall(call, headers);
    }

    private void refill(Bucket b) {
        long now = Instant.now().getEpochSecond();
        long delta = now - b.lastRefill;
        if (delta > 0) {
            int added = (int) Math.min((long)refillPerSec * delta, (long)capacity);
            b.tokens = Math.min(capacity, b.tokens + added);
            b.lastRefill = now;
        }
    }
}

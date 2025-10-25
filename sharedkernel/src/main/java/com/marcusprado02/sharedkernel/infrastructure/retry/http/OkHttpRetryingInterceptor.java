package com.marcusprado02.sharedkernel.infrastructure.retry.http;

import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import com.marcusprado02.sharedkernel.infrastructure.retry.*;

public class OkHttpRetryingInterceptor implements Interceptor {
    private final ThirdPartyRetryPolicy<Response, IOException> policy;

    public OkHttpRetryingInterceptor(ThirdPartyRetryPolicy<Response, IOException> policy) {
        this.policy = policy;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request orig = chain.request();
        boolean isWrite = !orig.method().equalsIgnoreCase("GET") && !orig.method().equalsIgnoreCase("HEAD");
        boolean idempotent = isWrite && hasIdempotencyKey(orig);

        RetryContext ctx = new RetryContext(
            "http-call", endpointKey(orig), tenant(orig), idempotencyKey(orig), null
        );

        int attempt = 1;
        Response lastResp = null;
        IOException lastErr = null;

        while (true) {
            Request req = ensureIdempotencyHeader(orig, isWrite);
            try {
                lastResp = chain.proceed(req);
                var dec = policy.decide(attempt, ctx, lastResp, null, isWrite, idempotent || hasIdempotencyKey(req));
                if (!dec.shouldRetry()) return lastResp;
                safeClose(lastResp);
                sleep(dec.backoffDelay());
                attempt = dec.nextAttempt();
            } catch (IOException ex) {
                lastErr = ex;
                var dec = policy.decide(attempt, ctx, null, ex, isWrite, idempotent || hasIdempotencyKey(req));
                if (!dec.shouldRetry()) throw ex;
                sleep(dec.backoffDelay());
                attempt = dec.nextAttempt();
            }
        }
    }

    private static void sleep(Duration d) {
        try { Thread.sleep(Math.max(0, d.toMillis())); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private static void safeClose(Response r){ if (r!=null && r.body()!=null) r.close(); }

    private static boolean hasIdempotencyKey(Request r) {
        return r.header("Idempotency-Key") != null;
    }

    private static UUID idempotencyKey(Request r) {
        String key = r.header("Idempotency-Key");
        return key != null ? UUID.fromString(key) : null;
    }

    private static Request ensureIdempotencyHeader(Request r, boolean isWrite) {
        if (!isWrite) return r;
        if (hasIdempotencyKey(r)) return r;
        return r.newBuilder().header("Idempotency-Key", UUID.randomUUID().toString()).build();
    }
    private static String endpointKey(Request r) {
        HttpUrl u = r.url();
        return u.host()+":"+u.port()+u.encodedPath(); // ex.: api.mapbox.com:443/geocoding/v5/...
    }
    private static String tenant(Request r) {
        return r.header("X-Tenant") != null ? r.header("X-Tenant") : "default";
    }
}

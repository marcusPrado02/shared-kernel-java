package com.marcusprado02.sharedkernel.observability.health.checks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.health.*;

public final class HttpProbe implements ProbeCheck {
    private final HttpClient client; 
    private final URI uri; 
    private final Duration timeout; 
    private final int expect;
    public HttpProbe(URI uri, Duration timeout, int expect){
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.uri = uri; this.timeout = timeout; this.expect = expect;
    }
    @Override public ProbeResult check() {
        var start = System.nanoTime();
        try {
            var req = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
            var res = client.send(req, HttpResponse.BodyHandlers.discarding());
            var dt = Duration.ofNanos(System.nanoTime()-start);
            if (res.statusCode()==expect) return ProbeResult.up(dt);
            if (res.statusCode()>=500) return ProbeResult.down("upstream_5xx", Map.of("code", res.statusCode()), dt);
            return ProbeResult.degraded("unexpected_status", Map.of("code", res.statusCode()), dt);
        } catch (Exception e) {
            var dt = Duration.ofNanos(System.nanoTime()-start);
            return ProbeResult.down("http_error", Map.of("error", e.getClass().getSimpleName(), "msg", e.getMessage()), dt);
        }
    }
    @Override public String name() { return "http"; }
}
package com.marcusprado02.sharedkernel.observability.logging.structured.sink;


import java.net.URI;
import java.net.http.*;
import java.time.Duration;

import com.marcusprado02.sharedkernel.observability.logging.structured.LogSink;

public final class HttpSink implements LogSink {
    private final HttpClient client;
    private final URI endpoint;
    private final String contentType;

    public HttpSink(String url, String contentType) {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.endpoint = URI.create(url);
        this.contentType = contentType==null? "application/json" : contentType;
    }

    @Override public void write(byte[] payload) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
        client.send(req, HttpResponse.BodyHandlers.discarding());
    }
}

package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;

public final class HttpSource implements ConfigSource {
  private final String url;
  private final String auth;
  private volatile String etag;

  private final HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(3)).build();

  public HttpSource(String url, String auth) { this.url = url; this.auth = auth; }

  @Override public Optional<ConfigSnapshot> load() {
    try {
      var reqBuilder = HttpRequest.newBuilder(URI.create(url))
          .timeout(Duration.ofSeconds(5))
          .GET();
      if (auth != null && !auth.isBlank()) reqBuilder.header("Authorization", auth);
      if (etag != null) reqBuilder.header("If-None-Match", etag);
      var resp = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() == 304 && etag != null) {
        return Optional.empty();
      }
      if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
        this.etag = resp.headers().firstValue("ETag").orElse(null);
        String ver = etag != null ? etag : String.valueOf(System.currentTimeMillis());
        return Optional.of(new ConfigSnapshot(ver, resp.body(), resp.headers().firstValue("Content-Type").orElse("application/x-yaml")));
      }
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override public String id() { return "http:" + url; }
}
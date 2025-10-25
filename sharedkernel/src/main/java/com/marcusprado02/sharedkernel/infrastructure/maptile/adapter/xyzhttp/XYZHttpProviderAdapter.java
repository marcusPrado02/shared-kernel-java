package com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.xyzhttp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderMetadata;

import com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.http.HttpClientFactory;
import com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.http.HttpHeaderParsers;

public class XYZHttpProviderAdapter extends BaseProviderAdapter {

    private final ProviderConfig cfg;
    private final HttpClient http;

    public XYZHttpProviderAdapter(ProviderConfig cfg, HttpClientFactory factory){
        this.cfg = cfg;
        this.http = (factory != null ? factory.create(cfg) : HttpClientFactory.defaultClient());
    }

    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata(
            "xyz-http", "XYZ HTTP Tiles", "1.0",
            Set.of(TileFormat.PNG, TileFormat.WEBP, TileFormat.PBF),
            Set.of("base","satellite","terrain"), Set.of("GLOBAL"),
            new Capabilities(true,true,true,true,true)
        );
    }

    @Override public TileData fetch(TileKey key, TileContext ctx, Policy policy) {
        return run(policy, () -> {
            String url = urlFor(key, ctx); // ex.: https://tiles.example.com/{z}/{x}/{y}{r}.png
            HttpRequest req = signed(url, key.retina(), cfg);

            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            int status = res.statusCode();
            if (status == 304) {
                // Sem corpo; deixe o cache externo decidir se usa o armazenado.
                throw new RuntimeException("Not Modified (304) sem cache local implementado");
            }
            if (status < 200 || status >= 300) {
                throw new RuntimeException("HTTP status: " + status);
            }

            String contentType = res.headers().firstValue("Content-Type").orElse(mimeFromFormat(key.format()));
            String etag = res.headers().firstValue("ETag").orElse(null);
            Instant lastModified = HttpHeaderParsers.lastModified(res.headers().firstValue("Last-Modified"));
            int ttl = HttpHeaderParsers.cacheTtlSeconds(
                    res.headers().firstValue("Cache-Control"),
                    res.headers().firstValue("Expires"));

            EtagInfo et = new EtagInfo(etag, lastModified.toString());
            TileMeta meta = new TileMeta(metadata().id(), et, ttl, System.currentTimeMillis());

            byte[] body = res.body();
            if (key.format() == TileFormat.PBF) return new VectorTile(body, meta);
            return new RasterTile(body, contentType, meta);
        });
    }

    @Override public CompletableFuture<TileData> fetchAsync(TileKey k, TileContext c, Policy p) {
        // Você pode delegar ao sendAsync do JDK para evitar um thread pool extra
        String url = urlFor(k, c);
        HttpRequest req = signed(url, k.retina(), cfg);

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(res -> {
                    int status = res.statusCode();
                    if (status < 200 || status >= 300)
                        throw new RuntimeException("HTTP status: " + status);

                    String contentType = res.headers().firstValue("Content-Type").orElse(mimeFromFormat(k.format()));
                    String etag = res.headers().firstValue("ETag").orElse(null);
                    Instant lastModified = HttpHeaderParsers.lastModified(res.headers().firstValue("Last-Modified"));
                    int ttl = HttpHeaderParsers.cacheTtlSeconds(
                            res.headers().firstValue("Cache-Control"),
                            res.headers().firstValue("Expires"));

                    EtagInfo et = new EtagInfo(etag, lastModified.toString());
                    TileMeta meta = new TileMeta(metadata().id(), et, ttl, System.currentTimeMillis());
                    byte[] body = res.body();
                    if (k.format() == TileFormat.PBF) return (TileData) new VectorTile(body, meta);
                    return (TileData) new RasterTile(body, contentType, meta);
                });
    }

    private String urlFor(TileKey k, TileContext ctx){
        String r = (k.retina() ? "@2x" : "");
        return cfg.template()
            .replace("{z}", String.valueOf(k.z()))
            .replace("{x}", String.valueOf(k.x()))
            .replace("{y}", String.valueOf(k.y()))
            .replace("{r}", r)
            .replace("{layer}", String.valueOf(ctx.layer()));
    }

    private HttpRequest signed(String url, boolean retina, ProviderConfig cfg){
        // Exemplo simples: só monta GET. Se precisar assinar query/headers, faça aqui.
        return HttpRequest.newBuilder(URI.create(url))
                .GET()
                // .header("If-None-Match", etag)
                // .header("Authorization", "Bearer "+cfg.get("token").orElse(""))
                .build();
    }

    private String mimeFromFormat(TileFormat f){
        return switch (f) {
            case JPEG -> "image/jpeg";
            case WEBP -> "image/webp";
            case PNG -> "image/png";
            case PBF -> "application/x-protobuf";
        };
    }
}
package com.marcusprado02.sharedkernel.adapters.in.sse.example.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.VersionNegotiator;
import com.marcusprado02.sharedkernel.adapters.in.sse.webflux.InProcSseSink;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/stream")
@RequiredArgsConstructor
public class SseController {
    private final InProcSseSink sink; // injete como @Bean
    private final VersionNegotiator versioner;

    @GetMapping(path="/{topic}", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<String>>> stream(@PathVariable String topic,
                                                     @RequestHeader(value="Last-Event-ID", required=false) String lastId,
                                                     ServerHttpResponse resp) {
        // Headers anti-buffer e cache
        var headers = resp.getHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.add("X-Accel-Buffering", "no");
        headers.add(HttpHeaders.CONNECTION, "keep-alive");
        headers.add(HttpHeaders.TRANSFER_ENCODING, "chunked");

        // (Opcional) Negocia versão do payload de data via VersionNegotiator
        // var decision = versioner.decide("events", null, null, null); headers.add("API-Version", decision.served().toString());

        var flux = sink.subscribe(topic, lastId)
                .onBackpressureDrop(sse -> {/* opcional: métrica drop */})
                .doOnCancel(() -> {/* métrica disconnect */});

        return Mono.just(ResponseEntity.ok().body(flux));
    }
}

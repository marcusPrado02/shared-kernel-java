package com.marcusprado02.sharedkernel.adapters.in.ws.bridge;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.marcusprado02.sharedkernel.adapters.in.ws.port.TopicBridge;
import com.marcusprado02.sharedkernel.contracts.ws.*;

public class InMemoryTopicBridge implements TopicBridge {
    private final Map<String, Sinks.Many<WsEvent<?>>> topics = new ConcurrentHashMap<>();

    @Override public Flux<WsEvent<?>> subscribe(String channel, String tenant, String subject, Map<String,Object> args) {
        var sink = topics.computeIfAbsent(channel, c -> Sinks.many().multicast().onBackpressureBuffer(1024));
        return sink.asFlux();
    }

    @Override public reactor.core.publisher.Mono<Void> publish(String channel, String tenant, String subject, String op, Map<String,Object> meta, Object payload) {
        var sink = topics.computeIfAbsent(channel, c -> Sinks.many().multicast().onBackpressureBuffer(1024));
        var ok = sink.tryEmitNext(new WsEvent<>(channel, op != null ? op : "event", payload, OffsetDateTime.now(), "v1"));
        if (ok.isFailure()) return reactor.core.publisher.Mono.error(new IllegalStateException("no-subs"));
        return reactor.core.publisher.Mono.empty();
    }
}


package com.marcusprado02.sharedkernel.adapters.in.ws.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.adapters.in.ws.guard.TokenBucket;
import com.marcusprado02.sharedkernel.adapters.in.ws.port.TopicBridge;
import com.marcusprado02.sharedkernel.adapters.in.ws.security.ChannelPolicy;
import com.marcusprado02.sharedkernel.adapters.in.ws.security.JwtVerifier;
import com.marcusprado02.sharedkernel.contracts.ws.WsAction;
import com.marcusprado02.sharedkernel.contracts.ws.WsMessage;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.BufferOverflowStrategy;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketGatewayHandler implements WebSocketHandler {

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final JwtVerifier jwt;
    private final ChannelPolicy policy;
    private final TopicBridge bridge;
    private final MeterRegistry metrics;
    private final TokenBucket rl = new TokenBucket(50, 25); // 50 tokens, 25/s

    // sessionId -> (channel -> subscription)
    private final Map<String, Map<String, Disposable>> subs = new ConcurrentHashMap<>();

    public WebSocketGatewayHandler(JwtVerifier jwt, ChannelPolicy policy, TopicBridge bridge, MeterRegistry metrics) {
        this.jwt = jwt; this.policy = policy; this.bridge = bridge; this.metrics = metrics;
    }

    @Override public Mono<Void> handle(WebSocketSession session) {
        var req = session.getHandshakeInfo().getHeaders();
        var principal = jwt.verify(session.getHandshakeInfo().getUri().getScheme().equals("ws")
                ? ((ServerHttpRequest) session.getAttributes().get("org.springframework.http.server.reactive.ServerHttpRequest")).mutate().build()
                : null);

        if (principal == null) {
            return session.send(Mono.just(session.textMessage(json(err("unauthorized","missing/invalid token")))))
                    .then(session.close(CloseStatus.NOT_ACCEPTABLE));
        }

        String sessId = session.getId();
        subs.putIfAbsent(sessId, new ConcurrentHashMap<>());

        // Outbound sink para “fanout” ao cliente, com proteção de slow consumer
        Sinks.Many<String> out = Sinks.many().unicast().onBackpressureBuffer();

        // Heartbeat: envia PONG a cada 20s se não houve tráfego
        var hb = Flux.interval(Duration.ofSeconds(20))
                .map(t -> json(new WsMessage<>(UUID.randomUUID().toString(), WsAction.PONG, null, null, null, Map.of())))
                .doOnNext(msg -> out.tryEmitNext(msg))
                .subscribe();

        // RECEBIMENTO
        var inbound = session.receive()
            .timeout(Duration.ofMinutes(2))
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap(txt -> onInbound(sessId, principal, txt, session, out))
            .onErrorResume(e -> {
                out.tryEmitNext(json(err("bad-request", e.getMessage())));
                return Mono.empty();
            });

        // ENVIO
        var outbound = session.send(out.asFlux().map(session::textMessage))
                .doFinally(sig -> cleanup(sessId, hb));

        return Mono.when(inbound, outbound);
    }

    private Mono<Void> onInbound(String sessId, JwtVerifier.Principal p, String txt, WebSocketSession session, Sinks.Many<String> out) {
        WsMessage<Map<String,Object>> msg;
        try {
            msg = om.readValue(txt, new TypeReference<>() {});
        } catch (Exception e) {
            metrics.counter("ws.messages.parse_error").increment();
            out.tryEmitNext(json(err("parse-error", "invalid json")));
            return Mono.empty();
        }

        // Rate-limit por subject+canal+action
        String rlKey = p.subject() + "|" + msg.action() + "|" + msg.channel();
        if (!rl.tryConsume(rlKey)) {
            out.tryEmitNext(json(err("rate-limit", "too many requests")));
            return Mono.empty();
        }

        switch (msg.action()) {
            case PING -> { out.tryEmitNext(json(new WsMessage<>(msg.id(), WsAction.PONG, null, null, null, Map.of("ack", true)))); return Mono.empty(); }
            case SUBSCRIBE -> { return doSubscribe(sessId, p, msg, out); }
            case UNSUBSCRIBE -> { return doUnsubscribe(sessId, msg, out); }
            case PUBLISH -> { return doPublish(p, msg, out); }
            default -> { out.tryEmitNext(json(err("unsupported", "action not supported"))); return Mono.empty(); }
        }
    }

    private Mono<Void> doSubscribe(String sessId, JwtVerifier.Principal p, WsMessage<Map<String,Object>> msg, Sinks.Many<String> out) {
        if (msg.channel() == null || msg.channel().isBlank()) { out.tryEmitNext(json(err("invalid","missing channel"))); return Mono.empty(); }
        if (!policy.canSubscribe(msg.channel(), p)) { out.tryEmitNext(json(err("forbidden","not allowed"))); return Mono.empty(); }

        // Cancela sub anterior
        doUnsubscribe(sessId, msg, out).subscribe();

        @SuppressWarnings("unchecked")
        Map<String,Object> args = (Map<String,Object>)(Map<?,?>)(msg.meta() != null ? msg.meta() : Map.of());

        var disposable = bridge.subscribe(msg.channel(), p.tenant(), p.subject(), args)
            .map(ev -> json(ev))
            .onBackpressureBuffer(256,
                dropped -> metrics.counter("ws.slow_consumer.drop", "channel", msg.channel()).increment(),
                BufferOverflowStrategy.DROP_OLDEST)
            .subscribe(payload -> {
                var res = new WsMessage<>(UUID.randomUUID().toString(), WsAction.PUBLISH, msg.channel(), "event", payload, Map.of());
                out.tryEmitNext(json(res));
                metrics.counter("ws.events.out", "channel", msg.channel()).increment();
            });

        subs.get(sessId).put(msg.channel(), disposable);
        out.tryEmitNext(json(ack(msg.id(), "subscribed")));
        metrics.counter("ws.subscribed", "channel", msg.channel()).increment();
        return Mono.empty();
    }


    private Mono<Void> doUnsubscribe(String sessId, WsMessage<?> msg, Sinks.Many<String> out) {
        var m = subs.get(sessId);
        if (m == null) return Mono.empty();
        var d = m.remove(msg.channel());
        if (d != null) { d.dispose(); out.tryEmitNext(json(ack(msg.id(), "unsubscribed"))); metrics.counter("ws.unsubscribed","channel", msg.channel()).increment(); }
        return Mono.empty();
    }

    private Mono<Void> doPublish(JwtVerifier.Principal p, WsMessage<Map<String,Object>> msg, Sinks.Many<String> out) {
        if (!policy.canPublish(msg.channel(), msg.op(), p)) { out.tryEmitNext(json(err("forbidden","not allowed"))); return Mono.empty(); }
        @SuppressWarnings("unchecked")
        Map<String,Object> meta = (Map<String,Object>)(Map<?,?>)(msg.meta() != null ? msg.meta() : Map.of());
        Object payload = msg.payload();

        return bridge.publish(msg.channel(), p.tenant(), p.subject(), msg.op(), meta, payload)
            .doOnSuccess(v -> { out.tryEmitNext(json(ack(msg.id(),"ok"))); metrics.counter("ws.publish.ok","channel",msg.channel()).increment(); })
            .doOnError(e -> out.tryEmitNext(json(err("publish-failed", e.getMessage()))))
            .onErrorResume(e -> Mono.empty());
    }


    private void cleanup(String sessId, Disposable hb) {
        hb.dispose();
        var m = subs.remove(sessId);
        if (m != null) m.values().forEach(Disposable::dispose);
    }

    private static Map<String,Object> ack(String id, String detail) {
        return Map.of("id", id, "status","ok","detail", detail);
    }
    private static Map<String,Object> err(String code, String detail) {
        return Map.of("error", Map.of("code", code, "detail", detail));
    }
    private String json(Object o) {
        try { return om.writeValueAsString(o); } catch (Exception e) { return "{\"error\":{\"code\":\"serialize\"}}"; }
    }
}

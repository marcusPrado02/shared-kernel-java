package com.marcusprado02.sharedkernel.adapters.in.sse.webflux;

import lombok.Builder;
import org.reactivestreams.Publisher;

import com.marcusprado02.sharedkernel.adapters.in.sse.core.*;

import reactor.core.publisher.*;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public final class InProcSseSink implements SseSink {
    @Builder
    public static final class Config {
        public int bufferPerClient = 256;           // itens em memória
        public Duration heartbeat = Duration.ofSeconds(15);
        public Duration clientTimeout = Duration.ofMinutes(5);
    }

    private final Config cfg;
    private final ConcurrentHashMap<String, Sinks.Many<SseMessage>> topics = new ConcurrentHashMap<>();

    public InProcSseSink(Config cfg){ this.cfg = cfg; }

    public Flux<String> subscribe(String topic, String lastEventId){
        var sink = topics.computeIfAbsent(topic, __ ->
            Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));
        var source = sink.asFlux();

        // Retomada básica: se você persistir últimos N eventos, reemita os > lastEventId aqui.
        Flux<SseMessage> stream = source
                .mergeWith(Flux.interval(cfg.heartbeat).map(i -> heartbeat()))
                .timeout(cfg.clientTimeout);

        // Encode SSE wire
        return stream.map(InProcSseSink::encode);
    }

    @Override public boolean emit(String topic, SseMessage msg) {
        var sink = topics.get(topic);
        if (sink == null) return false;
        var res = sink.tryEmitNext(msg);
        return res.isSuccess();
    }

    @Override public void closeTopic(String topic, String reason) {
        var sink = topics.remove(topic);
        if (sink != null) sink.tryEmitComplete();
    }

    private static SseMessage heartbeat(){
        return new SseMessage(null, null, "{}", null, java.time.Instant.now(), java.util.Map.of("hb","1"));
    }

    private static String encode(SseMessage m){
        var sb = new StringBuilder();
        if (m.id()!=null) sb.append("id: ").append(m.id()).append('\n');
        if (m.event()!=null) sb.append("event: ").append(m.event()).append('\n');
        // retry só precisa vir de vez em quando; aqui incluímos se presente
        if (m.retryMs()!=null) sb.append("retry: ").append(m.retryMs()).append('\n');
        // data deve ser linha única; se tiver quebras, substitua por espaços
        sb.append("data: ").append(m.data().replace('\n',' ')).append('\n').append('\n');
        return sb.toString();
    }
}

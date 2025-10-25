package com.marcusprado02.sharedkernel.adapters.in.sse.example.impl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.marcusprado02.sharedkernel.adapters.in.sse.core.SseMessage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/stream")
@RequiredArgsConstructor
public class SseServletController {
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.Set<SseEmitter>> subs = new java.util.concurrent.ConcurrentHashMap<>();

    @GetMapping(path="/{topic}")
    public SseEmitter stream(@PathVariable String topic,
                             @RequestHeader(value="Last-Event-ID", required=false) String lastId,
                             HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("X-Accel-Buffering", "no");
        var emitter = new SseEmitter(0L); // sem timeout; gerenciar manualmente
        subs.computeIfAbsent(topic, __ -> java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>()))
            .add(emitter);
        emitter.onCompletion(() -> subs.getOrDefault(topic, java.util.Set.of()).remove(emitter));
        emitter.onTimeout(() -> { emitter.complete(); });
        // opcional: reemitir backlog baseado em lastId
        return emitter;
    }

    // Fan-in local (exemplo simples)
    public void emit(String topic, SseMessage msg){
        var set = subs.getOrDefault(topic, java.util.Set.of());
        for (var em : set) {
            try {
                SseEmitter.SseEventBuilder ev = SseEmitter.event()
                        .id(msg.id()).name(msg.event()).data(msg.data());
                em.send(ev);
            } catch (Exception e){ em.complete(); }
        }
    }
}


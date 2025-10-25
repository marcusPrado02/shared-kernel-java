package com.marcusprado02.sharedkernel.crosscutting.hook.providers;


import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.crosscutting.hook.*;
import com.marcusprado02.sharedkernel.crosscutting.hook.spi.*;

public final class AuditLogHookProvider implements HookProvider {
    @Override public boolean supports(URI uri) {
        return "hook".equals(uri.getScheme()) && "audit".equals(uri.getHost());
    }

    @Override public HookRegistration<?> create(URI uri, Map<String, ?> defaults, Telemetry telemetry) {
        var q = query(uri);
        String topic = q.getOrDefault("topic", "default");
        int prio = Integer.parseInt(q.getOrDefault("priority","100"));
        Predicate<Object> filter = ev -> true;

        Hook<Object> handler = (event, ctx) -> {
            telemetry.count("audit.event", 1, Map.of("topic", ctx.topic()));
            // Aqui você pode publicar no EventStore/Kafka/DB
            // Exemplo minimalista: System.out.println(...)
            System.out.println("[AUDIT] topic="+ctx.topic()+" event="+String.valueOf(event));
        };

        var policy = ExecutionPolicy.builder()
                .attempts(Integer.parseInt(q.getOrDefault("attempts","3")))
                .timeout(Duration.ofMillis(Long.parseLong(q.getOrDefault("timeoutMs","500"))))
                .backoff(Duration.ofMillis(20), Duration.ofMillis(200))
                .isolate(true).swallowErrors(Boolean.parseBoolean(q.getOrDefault("swallow","true")))
                .build();

        return new HookRegistration<>(topic, HookPhase.AFTER, prio, (Predicate)filter, handler, policy);
    }

    private static Map<String,String> query(URI u){
        if (u.getQuery()==null) return Map.of();
        return java.util.Arrays.stream(u.getQuery().split("&"))
            .map(s->s.split("="))
            .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:""));
    }
}

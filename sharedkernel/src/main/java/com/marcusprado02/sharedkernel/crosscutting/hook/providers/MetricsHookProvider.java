package com.marcusprado02.sharedkernel.crosscutting.hook.providers;

import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.hook.*;
import com.marcusprado02.sharedkernel.crosscutting.hook.spi.*;

public final class MetricsHookProvider implements HookProvider {
    @Override public boolean supports(URI uri){ return "hook".equals(uri.getScheme()) && "metrics".equals(uri.getHost()); }

    @Override public HookRegistration<?> create(URI uri, Map<String, ?> defaults, Telemetry t) {
        var q = query(uri);
        String topic = q.getOrDefault("topic","default");
        String phase = q.getOrDefault("phase","before").toUpperCase();

        Hook<Object> h = (ev, ctx) -> t.count("callback."+phase.toLowerCase(), 1, Map.of("topic",topic));
        var policy = ExecutionPolicy.builder().attempts(1).swallowErrors(true).isolate(false).build();
        return new HookRegistration<>(topic, HookPhase.valueOf(phase), 10, e -> true, h, policy);
    }

    private static Map<String,String> query(URI u){
        if (u.getQuery()==null) return Map.of();
        return java.util.Arrays.stream(u.getQuery().split("&"))
            .map(s->s.split("="))
            .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:""));
    }
}

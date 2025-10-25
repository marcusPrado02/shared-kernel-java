package com.marcusprado02.sharedkernel.domain.events.upcast;


import java.util.List;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;
import com.marcusprado02.sharedkernel.domain.events.upcast.policies.FailPolicy;

public final class EventUpcasterChain {
    private final List<EventUpcaster> chain;
    private final FailPolicy failPolicy;
    private final FeatureFlagDecider ff;

    public EventUpcasterChain(List<EventUpcaster> chain, FailPolicy failPolicy, FeatureFlagDecider ff) {
        this.chain = chain; this.failPolicy = failPolicy; this.ff = ff;
    }

    public EventEnvelope upcastToLatest(EventEnvelope in, UpcastContext ctx) {
        if (!ff.enabled(in)) return in;

        EventEnvelope current = in;
        int safety = 256; // evita loops
        while (safety-- > 0) {
            var next = nextFor(current);
            if (next == null) return current; // já está na última versão conhecida
            var res = next.apply(current, ctx);
            if (res instanceof UpcastResult.Changed ch) {
                current = ch.envelope();
                continue;
            }
            if (res instanceof UpcastResult.Done done) {
                return done.envelope();
            }
            if (res instanceof UpcastResult.Skipped sk) {
                // Sem mudança; tenta próximo (ou encerra se não houver próximo)
                current = sk.envelope();
                // segue loop, pois pode haver outro upcaster subsequente da mesma fqn
                continue;
            }
            if (res instanceof UpcastResult.Failed f) {
                var decision = failPolicy.onFailure(f);
                if (decision instanceof UpcastResult.Skipped s) return s.envelope();
                if (decision instanceof UpcastResult.Failed) throw new RuntimeException("Upcast failed", f.error());
                current = ((UpcastResult.Changed)decision).envelope(); // incomum, mas suportado
            }
        }
        throw new IllegalStateException("Upcast chain exceeded safety bound");
    }

    private EventUpcaster nextFor(EventEnvelope e) {
        return chain.stream()
                .filter(u -> u.eventType().fqn().equals(e.metadata().eventType().fqn()))
                .filter(u -> u.fromVersion() == e.metadata().eventVersion())
                .findFirst().orElse(null);
    }
}

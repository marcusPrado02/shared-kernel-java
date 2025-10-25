package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.SpanHandle;

public final class DbSemanticDecorator implements TracingSpanDecorator {
    @Override public void afterStart(SpanHandle span, SpanConfig cfg) {
        // keys esperadas: db.system, db.name, db.user, net.peer.name, db.operation
        // exemplo de safe default:
        if (!cfg.attributes.containsKey("db.operation")) span.setAttribute("db.operation", "unknown");
    }
}
package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.SpanHandle;

public final class MessagingSemanticDecorator implements TracingSpanDecorator {
    @Override public void afterStart(SpanHandle span, SpanConfig cfg) {
        // keys: messaging.system, messaging.destination, messaging.operation (publish|receive|process)
        // default best-effort:
        if (!cfg.attributes.containsKey("messaging.operation")) span.setAttribute("messaging.operation", "process");
    }
}
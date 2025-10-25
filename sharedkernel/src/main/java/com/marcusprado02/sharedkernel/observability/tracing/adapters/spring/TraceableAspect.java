package com.marcusprado02.sharedkernel.observability.tracing.adapters.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.StatusCode;
import com.marcusprado02.sharedkernel.observability.tracing.TracingFacade;
import com.marcusprado02.sharedkernel.observability.tracing.annotation.Traceable;
import com.marcusprado02.sharedkernel.observability.tracing.decorators.TracingSpanDecorator;

import java.util.*;

@Aspect @Component
public class TraceableAspect {
    private final TracingFacade tracing;
    private final TracingSpanDecorator decorator;

    public TraceableAspect(TracingFacade tracing, TracingSpanDecorator decorator){
        this.tracing = tracing;
        this.decorator = decorator;
    }

    @Around("@annotation(t)")
    public Object around(ProceedingJoinPoint pjp, Traceable t) throws Throwable {
        SpanConfig cfg = SpanConfig.builder(t.name())
                .kind(t.kind())
                .attrs(parse(t.attributes()))
                .build();
        try (var span = tracing.startSpan(cfg)) {
            try {
                return pjp.proceed();
            } catch (Throwable ex) {
                if (t.captureExceptions()) {
                    if (decorator != null) decorator.onError(span, ex);
                    span.recordException(ex, Map.of("error.origin", "TraceableAspect"));
                    span.setStatus(StatusCode.ERROR, ex.getMessage());
                }
                throw ex;
            }
        }
    }

    private Map<String,Object> parse(String[] kv) {
        Map<String,Object> m = new LinkedHashMap<>();
        for (String s : kv) {
            if (s == null) continue;
            int i = s.indexOf(':');
            if (i>0) m.put(s.substring(0,i), s.substring(i+1));
        }
        return m;
    }
}
package com.marcusprado02.sharedkernel.crosscutting.aop.aspects;

import com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring.SpringInvocation;
import com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Timed;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.AspectBase;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Clock;
import java.time.Instant;

@Aspect
public class TimedAspect extends AspectBase {

    public TimedAspect(Tracer tracer, Meter meter, Clock clock, boolean failOpen) {
        super(tracer, meter, clock, failOpen);
    }

    @Around("@within(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Timed) || " +
            "@annotation(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Timed)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return super.invoke(new SpringInvocation(pjp));
    }

    @Override
    protected void before(Invocation inv, Span span) {
        // Marca no span que esta execução é “timed”
        inv.findAnnotation(Timed.class).ifPresent(t ->
            span.setAttribute("aop.timed.metric", t.metric())
        );
    }

    @Override
    protected void after(Invocation inv, Object result, Span span, Instant start) {
        // mantém métricas padrão do AspectBase
        super.after(inv, result, span, start);

        // adiciona métrica customizada do @Timed (histograma com nome configurável)
        inv.findAnnotation(Timed.class).ifPresent(t -> {
            long durationMs = Instant.now(clock).toEpochMilli() - start.toEpochMilli();
            var attrs = Attributes.builder()
                    .put("class", inv.getClassName())
                    .put("method", inv.getMethodName())
                    .put("metric", t.metric())
                    .build();

            // registra no nome pedido em @Timed.metric()
            meter.histogramBuilder(t.metric()).ofLongs().build().record(durationMs, attrs);

            // opcional: tags de alta cardinalidade (desative em produção se poluir)
            if (t.highCardinalityTags()) {
                var attrsHi = Attributes.builder()
                        .put("args.count", (long) inv.getArguments().length)
                        .build();
                meter.histogramBuilder(t.metric() + ".hc").ofLongs().build().record(durationMs, attrsHi);
            }
        });
    }
}

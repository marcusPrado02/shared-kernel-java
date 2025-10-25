package com.marcusprado02.sharedkernel.observability.logging.structured.adapter.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import com.marcusprado02.sharedkernel.observability.logging.*;
import com.marcusprado02.sharedkernel.observability.logging.impl.StandardLogEnricher;
import com.marcusprado02.sharedkernel.observability.logging.structured.*;
import com.marcusprado02.sharedkernel.observability.logging.structured.sink.ConsoleSink;

@Configuration
public class StructuredLogAutoConfig {

    @Bean
    public LogContext logContext(@Value("${spring.application.name:app}") String name,
                                 @Value("${app.env:unknown}") String env,
                                 @Value("${app.region:unknown}") String region,
                                 @Value("${app.version:unknown}") String version) {
        return new LogContext(name, env, region, version, java.util.Map.of("runtime", "jvm"));
    }

    @Bean
    public LogEnricher logEnricher() {
        var policy = FieldPolicy.permissive(); // do seu LogEnricher
        return (e, c) -> new StandardLogEnricher(policy, new Redactor(),
                CorrelationProvider.mdcDefault(), Sampler.always()).enrich(e, c);
    }

    @Bean(destroyMethod = "close")
    public StructuredLogger structuredLogger(LogEnricher enricher, LogContext ctx,
                                             @Value("${structuredlog.capacity:8192}") int capacity) {
        return StructuredLoggerFactory.builder()
                .name(ctx.service)
                .enricher(enricher)
                .context(ctx)
                .sink(new ConsoleSink())
                .capacity(capacity)
                .dropPolicy(AsyncStructuredLogger.DropPolicy.DROP_OLDEST)
                .rateLimiter(AsyncStructuredLogger.tokenBucket(500.0, 200))
                .build();
    }
}

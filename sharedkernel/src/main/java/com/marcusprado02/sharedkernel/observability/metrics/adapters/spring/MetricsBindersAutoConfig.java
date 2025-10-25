package com.marcusprado02.sharedkernel.observability.metrics.adapters.spring;


import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marcusprado02.sharedkernel.observability.metrics.bind.*;
import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

@Configuration
public class MetricsBindersAutoConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "metrics.binders.jvm", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CustomMeterBinder jvmMiniBinder(MetricsFacade metrics) {
        CustomMeterBinder b = new JvmMiniBinder();
        b.bindTo(metrics);
        return b;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "metrics.binders.poll", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ScheduledPollBinder pollBinder(MetricsFacade metrics) {
        var enricher = new MdcTagEnricher("tenant","trace_id","span_id","user");
        var binder = new ScheduledPollBinder(Duration.ofSeconds(15), enricher)
                .gauge(MetricId.builder("app","uptime.seconds").unit(Unit.SECONDS).build(),
                        () -> (System.currentTimeMillis() - START_TIME_MS)/1000.0,
                        MeterOptions.builder().build());
        binder.bindTo(metrics);
        return binder;
    }

    private static final long START_TIME_MS = System.currentTimeMillis();
}
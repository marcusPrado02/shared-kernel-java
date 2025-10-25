package com.marcusprado02.sharedkernel.observability.chaos.adapter.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;

import com.marcusprado02.sharedkernel.observability.chaos.ChaosEngine;
import com.marcusprado02.sharedkernel.observability.chaos.ChaosEngineImpl;
import com.marcusprado02.sharedkernel.observability.logging.structured.StructuredLogger;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.tracing.TracingFacade;

@Configuration
public class ChaosAutoConfig {

    @Bean @ConditionalOnMissingBean
    public ChaosEngine chaosEngine(MetricsFacade m, StructuredLogger s, TracingFacade t,
                                   @Value("${chaos.auth.token:}") String token){
        return new ChaosEngineImpl(m, s, t, token==null? "" : token);
    }

    @Bean
    public jakarta.servlet.Filter chaosHttpFilter(ChaosEngine engine,
                                                  @Value("${chaos.header.token:X-Chaos-Key}") String header){
        return new ChaosHttpFilter(engine, header);
    }

    @Bean
    public ChaoticAspect chaoticAspect(ChaosEngine engine){ return new ChaoticAspect(engine); }
}
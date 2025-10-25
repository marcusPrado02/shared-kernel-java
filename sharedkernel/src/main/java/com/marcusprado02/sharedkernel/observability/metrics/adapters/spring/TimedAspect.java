package com.marcusprado02.sharedkernel.observability.metrics.adapters.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.observability.metrics.annotation.Timed;
import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

import java.util.*;

@Aspect @Component
public class TimedAspect {
    private final MetricsFacade metrics;
    public TimedAspect(MetricsFacade metrics){ this.metrics = metrics; }

    @Around("@annotation(timed)")
    public Object around(ProceedingJoinPoint pjp, Timed timed) throws Throwable {
        MetricId id = MetricId.builder(timed.namespace(), timed.name())
                .unit(Unit.MILLISECONDS)
                .tags(parse(timed.tags()))
                .build();
        var opts = MeterOptions.builder().percentileHistogram(timed.histogram()).build();
        try (var ignored = metrics.timer(id, opts).time()) {
            return pjp.proceed();
        }
    }
    private Map<String,String> parse(String[] kv) {
        Map<String,String> m = new LinkedHashMap<>();
        for (String s : kv) {
            if (s == null) continue;
            int i = s.indexOf(':');
            if (i>0) m.put(s.substring(0,i), s.substring(i+1));
        }
        return m;
    }
}
package com.marcusprado02.sharedkernel.observability.tracing.samplers.presets;


import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.*;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.rule.*;

public final class Samplers {
    private Samplers(){}

    public static EventSampler defaultProd(){
        return new CompositeEventSampler(List.of(
                // 1) Sempre manter WARN/ERROR/FATAL
                new PercentRule(0.0, Map.of("WARN",1.0,"ERROR",1.0,"FATAL",1.0)),
                // 2) Erros/outliers/SLO
                new ErrorAndSloRule(Set.of("ERROR","FATAL","WARN"), 0.2, 2.5),
                // 3) 10% por rota/chave
                new PerKeyHashRule(0.10),
                // 4) Rate limit global (500/s com burst 200) e por rota (50/s)
                new RateLimitRule(500, 200, 50, 20),
                // 5) Janelas de pico (madrugada: 50%)
                new TimeWindowRule(LocalTime.of(0,0), LocalTime.of(6,0), 0.5),
                // 6) Probabilidade residual (1%)
                new PercentRule(0.01, Map.of())
        ), true); // defaultDrop = true
    }
}
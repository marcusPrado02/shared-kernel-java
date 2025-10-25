package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit;

import java.time.Duration;

public record LimitSpec(
        String name,
        int capacity,              // limite/janela
        Duration window,           // tamanho da janela (p.ex. 1s, 10s, 1m)
        int localBurst,            // tokens no bucket local (absorve janelas de RTT)
        boolean blockOnExceed,     // true=429, false=shadow (só marca)
        Duration penalty,          // se exceder N vezes, aplica cooldown
        int penaltyThreshold       // N violações até ativar penalty
) {}


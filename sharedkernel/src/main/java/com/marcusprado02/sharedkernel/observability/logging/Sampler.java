package com.marcusprado02.sharedkernel.observability.logging;

import java.util.concurrent.ThreadLocalRandom;

public interface Sampler {
    /** Retorna true se deve logar. */
    boolean accept(Severity severity, String loggerName, String message);

    static Sampler percent(double p) {
        double rate = Math.max(0.0, Math.min(1.0, p));
        return (sev, log, msg) -> ThreadLocalRandom.current().nextDouble() < rate || sev.ordinal() >= Severity.WARN.ordinal();
    }

    static Sampler always(){ return (s,l,m) -> true; }
}

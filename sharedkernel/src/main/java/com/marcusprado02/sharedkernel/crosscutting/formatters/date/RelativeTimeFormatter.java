package com.marcusprado02.sharedkernel.crosscutting.formatters.date;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class RelativeTimeFormatter implements Formatter<Instant> {
    private final Clock clock;
    public RelativeTimeFormatter(Clock clock) { this.clock = clock; }
    @Override
    public String format(Instant v) {
        long diffSec = Duration.between(v, clock.instant()).getSeconds();
        if (diffSec < 60) return diffSec + "s atrás";
        if (diffSec < 3600) return (diffSec/60) + "m atrás";
        if (diffSec < 86400) return (diffSec/3600) + "h atrás";
        return (diffSec/86400) + "d atrás";
    }
}

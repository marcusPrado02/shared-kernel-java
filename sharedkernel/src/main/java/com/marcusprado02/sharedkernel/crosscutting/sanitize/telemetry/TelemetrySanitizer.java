package com.marcusprado02.sharedkernel.crosscutting.sanitize.telemetry;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class TelemetrySanitizer<T> implements Sanitizer<T> {
    private final Sanitizer<T> delegate; private final Telemetry t; private final String prefix;
    public TelemetrySanitizer(Sanitizer<T> d, Telemetry t, String prefix){ this.delegate=d; this.t=t; this.prefix=prefix; }
    @Override public T sanitize(T in, SanitizationContext ctx) {
        try {
            return t.time(prefix + ".latency", () -> {
                T out = delegate.sanitize(in, ctx);
                boolean changed = (in==null && out!=null) || (in!=null && !in.equals(out));
                t.count(prefix + (changed?".changed":".unchanged"), 1);
                return out;
            });
        } catch (RuntimeException re){ t.count(prefix + ".error", 1); throw re; }
        catch (Exception e){ t.count(prefix + ".error", 1); throw new SanitizationException("obs failed", e); }
    }
}

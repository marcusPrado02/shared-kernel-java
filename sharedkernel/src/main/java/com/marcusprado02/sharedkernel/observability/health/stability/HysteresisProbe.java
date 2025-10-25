package com.marcusprado02.sharedkernel.observability.health.stability;

import com.marcusprado02.sharedkernel.observability.health.*;

/** Se DOWN intermitente, exige K confirmações consecutivas para trocar estado */
public final class HysteresisProbe implements ProbeCheck {
    private final ProbeCheck delegate;
    private final int kConfirmations;
    private Status lastStable = Status.UP;
    private int pending = 0;

    public HysteresisProbe(ProbeCheck d, int k) { delegate=d; kConfirmations=k; }

    @Override public ProbeResult check() {
        var r = delegate.check();
        if (r.status() != lastStable) {
            pending++;
            if (pending >= kConfirmations) { lastStable = r.status(); pending = 0; }
            // enquanto estabiliza, reporta o estável mas com hint
            return new ProbeResult(lastStable, "transitioning", r.details(), r.time());
        }
        pending = 0;
        return r;
    }
    @Override public String name() { return delegate.name(); }
}
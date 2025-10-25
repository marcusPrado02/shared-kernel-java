package com.marcusprado02.sharedkernel.cqrs.timemachine;

import com.marcusprado02.sharedkernel.cqrs.bus.EventEnvelope;
import com.marcusprado02.sharedkernel.cqrs.bus.ReplayCursor;

public interface ReplayCheckpoint {
    ReplayCursor cursorOr(ReplayCursor fallback);
    void update(EventEnvelope lastEv);
}


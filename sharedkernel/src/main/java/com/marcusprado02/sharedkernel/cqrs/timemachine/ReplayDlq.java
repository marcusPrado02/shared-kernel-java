package com.marcusprado02.sharedkernel.cqrs.timemachine;

import com.marcusprado02.sharedkernel.cqrs.bus.EventEnvelope;

public interface ReplayDlq {
    void put(EventEnvelope evt, String reason, Throwable error);
}

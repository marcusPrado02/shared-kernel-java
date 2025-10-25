package com.marcusprado02.sharedkernel.domain.events.upcast;

import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public final class FeatureFlagDecider {
    private final Predicate<EventEnvelope> predicate;
    public FeatureFlagDecider(Predicate<EventEnvelope> predicate){ this.predicate = predicate; }
    public boolean enabled(EventEnvelope e){ return predicate.test(e); }
}
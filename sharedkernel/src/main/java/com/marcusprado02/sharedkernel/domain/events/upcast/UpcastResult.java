package com.marcusprado02.sharedkernel.domain.events.upcast;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public sealed interface UpcastResult permits UpcastResult.Done, UpcastResult.Changed, UpcastResult.Skipped, UpcastResult.Failed {
    record Done(EventEnvelope envelope) implements UpcastResult {}
    record Changed(EventEnvelope envelope, String reason) implements UpcastResult {}
    record Skipped(EventEnvelope envelope, String reason) implements UpcastResult {}
    record Failed(EventEnvelope envelope, String reason, Throwable error) implements UpcastResult {}
}

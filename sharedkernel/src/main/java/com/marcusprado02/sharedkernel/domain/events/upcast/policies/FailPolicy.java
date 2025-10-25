package com.marcusprado02.sharedkernel.domain.events.upcast.policies;

import com.marcusprado02.sharedkernel.domain.events.upcast.UpcastResult;

public interface FailPolicy {
    UpcastResult onFailure(UpcastResult.Failed failed);
    static FailPolicy failFast() { return failed -> failed; }
    static FailPolicy logAndSkip(java.util.function.Consumer<UpcastResult.Failed> logger) {
        return failed -> { logger.accept(failed); return new UpcastResult.Skipped(failed.envelope(), "failure_skip"); };
    }
}

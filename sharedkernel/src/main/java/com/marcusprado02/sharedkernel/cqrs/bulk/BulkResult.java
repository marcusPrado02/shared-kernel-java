package com.marcusprado02.sharedkernel.cqrs.bulk;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public record BulkResult<R>(
        int total, int succeeded, int failed,
        Duration elapsed,
        List<ItemResult<R>> items
) {
    public record ItemResult<R>(int index, String itemId, Status status, Optional<R> value, Optional<Throwable> error, long tookMillis) {
        public enum Status { COMPLETED, REJECTED, FAILED, SKIPPED }
    }
}

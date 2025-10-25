package com.marcusprado02.sharedkernel.infrastructure.cdn;

import java.util.concurrent.CompletableFuture;

public interface CDNInvalidator {
    InvalidateResponse invalidate(InvalidateRequest req);
    CompletableFuture<InvalidateResponse> invalidateAsync(InvalidateRequest req);
    String backendName();
}
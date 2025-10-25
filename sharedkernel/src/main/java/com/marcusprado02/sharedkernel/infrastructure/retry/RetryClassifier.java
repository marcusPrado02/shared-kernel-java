package com.marcusprado02.sharedkernel.infrastructure.retry;

public interface RetryClassifier<R, E extends Throwable> {
    RetryCategory classify(R response, E error);
    default long retryAfterMillis(R response) { return -1L; } // extrai Retry-After se houver
}
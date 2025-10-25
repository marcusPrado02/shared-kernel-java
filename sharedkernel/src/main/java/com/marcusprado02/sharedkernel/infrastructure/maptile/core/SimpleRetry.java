package com.marcusprado02.sharedkernel.infrastructure.maptile.core;


import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Implementação básica de RetryPolicy que tenta novamente um número fixo de vezes,
 * aguardando um intervalo entre as tentativas.
 */
class SimpleRetry implements RetryPolicy {

    private final Duration delay;
    private final int maxAttempts;

    SimpleRetry(Duration delay, int maxAttempts) {
        this.delay = delay;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public <T> T executeWithRetry(Callable<T> c) {
        RuntimeException lastEx = null;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return c.call();
            } catch (Exception e) {
                lastEx = new RuntimeException(e);
                if (i < maxAttempts - 1) {
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw lastEx != null ? lastEx : new RuntimeException("Retry failed without exception?");
    }
}
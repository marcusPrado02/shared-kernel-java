package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;

public class RetryDecorator<I,O> extends PortDecorator<I,O> {
    private final int maxAttempts;
    private final long baseBackoffMs;
    private final java.util.function.Predicate<Throwable> retryOn;
    private final java.util.function.Predicate<I> isIdempotent;

    public RetryDecorator(Port<I,O> delegate, int maxAttempts, long baseBackoffMs,
                          java.util.function.Predicate<Throwable> retryOn,
                          java.util.function.Predicate<I> isIdempotent) {
        super(delegate);
        this.maxAttempts = maxAttempts;
        this.baseBackoffMs = baseBackoffMs;
        this.retryOn = retryOn;
        this.isIdempotent = isIdempotent;
    }

    @Override
    public O execute(I input) throws Exception {
        if (!isIdempotent.test(input)) return delegate.execute(input);
        int attempts = 0;
        long backoff = baseBackoffMs;
        while (true) {
            attempts++;
            try {
                return delegate.execute(input);
            } catch (Exception e) {
                if (attempts >= maxAttempts || !retryOn.test(e)) throw e;
                long jitter = java.util.concurrent.ThreadLocalRandom.current().nextLong(backoff / 4 + 1);
                Thread.sleep(Math.min(5000, backoff + jitter));
                backoff = Math.min(5000, backoff * 2);
            }
        }
    }
}


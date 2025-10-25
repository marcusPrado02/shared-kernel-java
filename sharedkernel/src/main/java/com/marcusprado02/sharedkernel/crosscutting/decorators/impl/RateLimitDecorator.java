package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;

public class RateLimitDecorator<I,O> extends PortDecorator<I,O> {
    private final io.github.resilience4j.ratelimiter.RateLimiter rl;
    private final java.util.function.Function<I,String> keyFn;

    public RateLimitDecorator(Port<I,O> delegate, io.github.resilience4j.ratelimiter.RateLimiter rl,
                              java.util.function.Function<I,String> keyFn) {
        super(delegate); this.rl = rl; this.keyFn = keyFn;
    }

    @Override
    public O execute(I input) throws Exception {
        // se quiser por chave, use um registry de RateLimiter por keyFn.apply(input)
        try {
            return io.github.resilience4j.ratelimiter.RateLimiter
                    .decorateCheckedSupplier(rl, () -> delegate.execute(input)).get();
        } catch (Throwable e) {
            if (e instanceof Exception ex) throw ex;
            throw new RuntimeException(e);
        }
    }
}


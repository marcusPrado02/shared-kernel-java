package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;
import com.marcusprado02.sharedkernel.crosscutting.idempotency.IdempotencyStore;

public class IdempotencyDecorator<I,O> extends PortDecorator<I,O> {
    private final IdempotencyStore store;
    private final java.util.function.Function<I,String> keyFn;
    private final long ttlSec;

    public IdempotencyDecorator(Port<I,O> delegate, IdempotencyStore store,
                                java.util.function.Function<I,String> keyFn, long ttlSec) {
        super(delegate); this.store = store; this.keyFn = keyFn; this.ttlSec = ttlSec;
    }

    @Override @SuppressWarnings("unchecked")
    public O execute(I input) throws Exception {
        String key = keyFn.apply(input);
        var hit = store.tryGet(key);
        if (hit.isPresent()) return (O) hit.get();
        O out = delegate.execute(input);
        store.put(key, out, ttlSec);
        return out;
    }
}

package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.cache.CacheStore;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;

public class CacheDecorator<I,O> extends PortDecorator<I,O> {
    private final java.util.function.Function<I,String> cacheKeyFn;
    private final java.util.function.Function<O,Long> ttlFn;
    private final CacheStore store; // get/set(key, value, ttlSec)

    public CacheDecorator(Port<I,O> delegate, CacheStore store,
                          java.util.function.Function<I,String> keyFn,
                          java.util.function.Function<O,Long> ttlFn) {
        super(delegate);
        this.store = store; this.cacheKeyFn = keyFn; this.ttlFn = ttlFn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public O execute(I input) throws Exception {
        String key = cacheKeyFn.apply(input);
        Object cached = store.get(key);
        if (cached != null) return (O) cached;

        O res = delegate.execute(input);
        Long ttl = (ttlFn != null ? ttlFn.apply(res) : null);
        if (ttl != null && ttl > 0) store.set(key, res, ttl);
        return res;
    }
}
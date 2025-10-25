package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.IntStream;

@FunctionalInterface
public interface Generator<T> {
    T generate(GenerationContext ctx) throws GenerationException;

    default List<T> batch(GenerationContext ctx, int n) {
        if (n < 0) throw new IllegalArgumentException("n<0");
        return IntStream.range(0, n).mapToObj(i -> generate(ctx)).toList();
    }

    default CompletableFuture<T> generateAsync(GenerationContext ctx, Executor executor) {
        return CompletableFuture.supplyAsync(() -> generate(ctx), executor);
    }

    default Publisher<T> stream(GenerationContext ctx, long items) {
        return subscriber -> subscriber.onSubscribe(new Subscription() {
            volatile boolean cancelled=false; long remaining=items;
            public void request(long n) {
                if (cancelled) return;
                long req = Math.min(n, remaining);
                try {
                    for (int i=0;i<req;i++) {
                        subscriber.onNext(generate(ctx));
                        remaining--;
                        if (remaining==0){ subscriber.onComplete(); break; }
                    }
                } catch (Throwable t){ subscriber.onError(t); }
            }
            public void cancel() { cancelled=true; }
        });
    }
}


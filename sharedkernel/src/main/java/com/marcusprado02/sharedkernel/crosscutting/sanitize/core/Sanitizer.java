package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

@FunctionalInterface
public interface Sanitizer<T> {
    T sanitize(T input, SanitizationContext ctx) throws SanitizationException;

    default List<T> batch(List<T> inputs, SanitizationContext ctx){
        return inputs.stream().map(v -> sanitize(v, ctx)).toList();
    }
    default CompletableFuture<T> sanitizeAsync(T input, SanitizationContext ctx, Executor executor){
        return CompletableFuture.supplyAsync(() -> sanitize(input, ctx), executor);
    }
    default Publisher<T> stream(Publisher<T> in, SanitizationContext ctx){
        return subscriber -> in.subscribe(new Subscriber<>() {
            @Override public void onSubscribe(Subscription s){ subscriber.onSubscribe(s); }
            @Override public void onNext(T item){ subscriber.onNext(sanitize(item, ctx)); }
            @Override public void onError(Throwable t){ subscriber.onError(t); }
            @Override public void onComplete(){ subscriber.onComplete(); }
        });
    }
}
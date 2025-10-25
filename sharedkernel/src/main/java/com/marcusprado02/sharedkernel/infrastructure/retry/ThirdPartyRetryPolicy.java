package com.marcusprado02.sharedkernel.infrastructure.retry;


import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public final class ThirdPartyRetryPolicy<R, E extends Throwable> {

    public enum Backoff { EXPONENTIAL, EQUAL_JITTER, DECORRELATED_JITTER }

    private final int maxAttempts;                 // incl. a primeira tentativa
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Backoff backoff;
    private final boolean honorRetryAfter;
    private final boolean requireIdempotencyForWrites;
    private final RetryClassifier<R,E> classifier;
    private final RetryBudget budget;              // token bucket p/ evitar storms
    private final BiFunction<Integer, Duration, Duration> timeLimiter; // opcional p/ cap por SLA

    private ThirdPartyRetryPolicy(Builder<R,E> b) {
        this.maxAttempts = b.maxAttempts;
        this.baseDelay = b.baseDelay;
        this.maxDelay = b.maxDelay;
        this.backoff = b.backoff;
        this.honorRetryAfter = b.honorRetryAfter;
        this.requireIdempotencyForWrites = b.requireIdempotencyForWrites;
        this.classifier = b.classifier;
        this.budget = b.budget;
        this.timeLimiter = b.timeLimiter;
    }

    public RetryDecision decide(int attempt, RetryContext ctx, R response, E error, boolean isWrite, boolean idempotent) {
        var cat = classifier.classify(response, error);
        if (cat == RetryCategory.SUCCESS) return RetryDecision.stop("success");
        if (attempt >= maxAttempts) return RetryDecision.stop("exceeded maxAttempts");
        if (!budget.tryConsume(ctx)) return RetryDecision.stop("retry budget exhausted");

        if (cat == RetryCategory.UNRETRYABLE || cat == RetryCategory.FATAL)
            return RetryDecision.stop("unretryable/fatal");

        if (isWrite && requireIdempotencyForWrites && !idempotent)
            return RetryDecision.stop("non-idempotent write");

        long ra = honorRetryAfter ? Math.max(0, classifier.retryAfterMillis(response)) : -1;
        Duration delay = ra >= 0 ? Duration.ofMillis(ra) : computeBackoff(attempt);
        if (timeLimiter != null) delay = timeLimiter.apply(attempt, delay);
        return RetryDecision.retry(attempt + 1, delay, cat.name().toLowerCase());
    }

    private Duration computeBackoff(int attempt) {
        long base = baseDelay.toMillis();
        long cap  = maxDelay.toMillis();
        long d;
        switch (backoff) {
            case EXPONENTIAL -> d = Math.min(cap, (long)(base * Math.pow(2, attempt-1)));
            case EQUAL_JITTER -> {
                long exp = Math.min(cap, (long)(base * Math.pow(2, attempt-1)));
                d = (exp / 2) + ThreadLocalRandom.current().nextLong(exp / 2 + 1);
            }
            case DECORRELATED_JITTER -> {
                // d = min(cap, randomBetween(base, prev*3)); prev estimado por attempt
                long prev = Math.min(cap, (long)(base * Math.pow(2, Math.max(0, attempt-2))));
                long next = base + ThreadLocalRandom.current().nextLong(Math.max(base, prev * 3) - base + 1);
                d = Math.min(cap, next);
            }
            default -> d = base;
        }
        return Duration.ofMillis(Math.max(base, d));
    }

    // ---------- Builder ----------
    public static final class Builder<R,E extends Throwable> {
        private int maxAttempts = 4;
        private Duration baseDelay = Duration.ofMillis(150);
        private Duration maxDelay  = Duration.ofSeconds(2);
        private Backoff backoff = Backoff.EQUAL_JITTER;
        private boolean honorRetryAfter = true;
        private boolean requireIdempotencyForWrites = true;
        private RetryClassifier<R,E> classifier;
        private RetryBudget budget = RetryBudget.unlimited();
        private BiFunction<Integer,Duration,Duration> timeLimiter;

        public Builder<R,E> maxAttempts(int v){ this.maxAttempts=v; return this; }
        public Builder<R,E> baseDelay(Duration d){ this.baseDelay=d; return this; }
        public Builder<R,E> maxDelay(Duration d){ this.maxDelay=d; return this; }
        public Builder<R,E> backoff(Backoff b){ this.backoff=b; return this; }
        public Builder<R,E> honorRetryAfter(boolean v){ this.honorRetryAfter=v; return this; }
        public Builder<R,E> requireIdempotencyForWrites(boolean v){ this.requireIdempotencyForWrites=v; return this; }
        public Builder<R,E> classifier(RetryClassifier<R,E> c){ this.classifier= Objects.requireNonNull(c); return this; }
        public Builder<R,E> budget(RetryBudget b){ this.budget=Objects.requireNonNull(b); return this; }
        public Builder<R,E> timeLimiter(BiFunction<Integer,Duration,Duration> tl){ this.timeLimiter=tl; return this; }
        public ThirdPartyRetryPolicy<R,E> build(){ return new ThirdPartyRetryPolicy<>(this); }
    }
}
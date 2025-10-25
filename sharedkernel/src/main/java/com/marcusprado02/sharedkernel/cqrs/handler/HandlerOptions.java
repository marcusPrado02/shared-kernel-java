package com.marcusprado02.sharedkernel.cqrs.handler;

import java.time.Duration;

public record HandlerOptions(
        boolean transactional,
        boolean publishDomainEvents,
        boolean useOutbox,
        int retryMaxAttempts,
        Duration retryInitialBackoff,
        double retryBackoffMultiplier,
        boolean retryJitter,
        boolean enforceAuthorization,
        boolean enforceBusinessValidation
) {
    public static HandlerOptions defaultStrong(){
        return new HandlerOptions(
            true,  // transactional
            true,  // publishDomainEvents
            true,  // useOutbox
            1,     // retryMaxAttempts
            Duration.ZERO, 1.0, false,
            true,  // enforceAuthorization
            true   // enforceBusinessValidation
        );
    }
    public HandlerOptions withRetry(int attempts, Duration initial, double mult, boolean jitter){
        return new HandlerOptions(transactional, publishDomainEvents, useOutbox, attempts, initial, mult, jitter, enforceAuthorization, enforceBusinessValidation);
    }
}

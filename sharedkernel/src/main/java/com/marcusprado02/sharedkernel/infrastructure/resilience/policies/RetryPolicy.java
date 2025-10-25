package com.marcusprado02.sharedkernel.infrastructure.resilience.policies;

import java.time.Duration;
import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;

public interface RetryPolicy extends Policy {
  int maxAttempts();
  Duration baseDelay();
  BackoffStrategy backoff();
  Predicate<Throwable> retryOn();
}

package com.marcusprado02.sharedkernel.domain.policy.enforcement;


import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class CircuitBreaker {
  public enum State { CLOSED, OPEN, HALF_OPEN }
  private final int failureThreshold;
  private final Duration openDuration;
  private final AtomicInteger failures = new AtomicInteger();
  private volatile long openUntil = 0L;
  private volatile State state = State.CLOSED;

  public CircuitBreaker(int failureThreshold, Duration openDuration) {
    this.failureThreshold = failureThreshold; this.openDuration = openDuration;
  }

  public synchronized boolean allow() {
    if (state == State.OPEN && System.currentTimeMillis() >= openUntil) state = State.HALF_OPEN;
    return state != State.OPEN;
  }

  public synchronized void onSuccess() {
    failures.set(0); state = State.CLOSED;
  }

  public synchronized void onFailure() {
    if (failures.incrementAndGet() >= failureThreshold) {
      state = State.OPEN; openUntil = System.currentTimeMillis() + openDuration.toMillis();
    }
  }
}


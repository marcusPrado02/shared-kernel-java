package com.marcusprado02.sharedkernel.infrastructure.resilience.policies;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;

public interface CircuitBreakerPolicy extends Policy {
  enum State { CLOSED, OPEN, HALF_OPEN }
  State state();
}
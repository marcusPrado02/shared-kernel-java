package com.marcusprado02.sharedkernel.infrastructure.resilience.policies;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;

public interface BulkheadPolicy extends Policy {
  int maxConcurrent();
  int queueSize();
}

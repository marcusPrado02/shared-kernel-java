package com.marcusprado02.sharedkernel.infrastructure.resilience.policies;

import java.time.Duration;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;

public interface TimeoutPolicy extends Policy { Duration timeout(); }

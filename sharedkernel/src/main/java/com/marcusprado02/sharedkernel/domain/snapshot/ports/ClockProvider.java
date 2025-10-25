package com.marcusprado02.sharedkernel.domain.snapshot.ports;

import java.time.Clock;

@FunctionalInterface
public interface ClockProvider { Clock clock(); }

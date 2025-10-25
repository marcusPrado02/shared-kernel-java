package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.time.Instant;

public interface TimeProvider { Instant now(); }
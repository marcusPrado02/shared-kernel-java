package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.time.Instant;

public interface TemporalTask {
  String id();
  Instant scheduledAt();
  void run(TimeProvider clock) throws Exception; // usa clock virtual!
}

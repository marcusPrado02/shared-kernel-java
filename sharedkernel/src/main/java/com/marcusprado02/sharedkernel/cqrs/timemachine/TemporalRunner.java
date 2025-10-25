package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class TemporalRunner {
  private final PriorityQueue<TemporalTask> queue = new PriorityQueue<>(Comparator.comparing(TemporalTask::scheduledAt));
  private final VirtualClock clock;

  public TemporalRunner(VirtualClock clock){ this.clock = clock; }
  public void submit(TemporalTask t){ queue.offer(t); }
  /** Avança o tempo e dispara todas as tarefas vencidas. */
  public void advanceTo(Instant instant) throws Exception {
    clock.freezeAt(instant);
    while (!queue.isEmpty() && !queue.peek().scheduledAt().isAfter(instant)) {
      var t = queue.poll(); t.run(clock);
    }
  }
}

package com.marcusprado02.sharedkernel.cqrs.async;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.adapters.out.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.cqrs.command.Command;

public interface AsyncCommandScheduler {
  String scheduleAt(MessageEnvelope<? extends Command> cmd, Instant whenUtc, ScheduleOptions opts);
  String scheduleAfter(MessageEnvelope<? extends Command> cmd, Duration delay, ScheduleOptions opts);
  String scheduleCron(MessageEnvelope<? extends Command> cmd, String cronExpr, ZoneId tz, ScheduleOptions opts);
  boolean cancel(String scheduleId);
  boolean reschedule(String scheduleId, Instant newWhenUtc);
  Optional<ScheduledInfo> get(String scheduleId);

  record ScheduleOptions(
      String idempotencyKey,              // opcional; se nulo, usa scheduleId
      int maxAttempts,                    // p/ retires do dispatcher
      Duration backoff,                   // em falhas transientes
      Map<String, String> tags,           // observabilidade (tenant, feature)
      MisfirePolicy misfirePolicy         // FireNow | Ignore | NextWithExistingCount
  ) {
    public static ScheduleOptions defaults() {
      return new ScheduleOptions(null, 5, Duration.ofSeconds(5), Map.of(), MisfirePolicy.FIRE_NOW);
    }
  }
  enum MisfirePolicy { FIRE_NOW, IGNORE, NEXT_WITH_EXISTING_COUNT }
  record ScheduledInfo(String id, Instant nextFireTime, String state, Map<String,String> tags) {}
}


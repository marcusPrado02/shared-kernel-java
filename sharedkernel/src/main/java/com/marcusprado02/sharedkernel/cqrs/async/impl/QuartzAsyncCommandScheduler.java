package com.marcusprado02.sharedkernel.cqrs.async.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.adapters.out.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.cqrs.async.AsyncCommandScheduler;
import com.marcusprado02.sharedkernel.cqrs.async.AsyncCommandScheduler.ScheduleOptions;
import com.marcusprado02.sharedkernel.cqrs.async.AsyncCommandScheduler.ScheduledInfo;
import com.marcusprado02.sharedkernel.cqrs.command.Command;

// Quartz
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

public class QuartzAsyncCommandScheduler implements AsyncCommandScheduler {

  private final Scheduler scheduler;
  private final ObjectMapper mapper;

  public QuartzAsyncCommandScheduler(Scheduler scheduler, ObjectMapper mapper) {
    this.scheduler = scheduler;
    this.mapper = mapper;
  }

  // ======= Assinaturas DEVEM casar com a interface (<? extends Command>) =======

  @Override
  public String scheduleAt(MessageEnvelope<? extends Command> cmd, Instant whenUtc, ScheduleOptions opts) {
    try {
      var id = UUID.randomUUID().toString();
      var jobKey = jobKey(id, cmd);

      // Empacotar dados no JobDataMap (não passe Map direto para usingJobData)
      var jdm = new JobDataMap();
      jdm.put(DispatchCommandJob.CMD_JSON, mapper.writeValueAsString(cmd));
      jdm.put(DispatchCommandJob.TENANT, cmd.tenantId());
      jdm.put(DispatchCommandJob.IDEM_KEY, Optional.ofNullable(opts.idempotencyKey()).orElse(null));
      // Para tags, serialize como JSON (ou mude o Job para aceitar JobDataMap aninhado)
      jdm.put(DispatchCommandJob.TAGS, mapper.writeValueAsString(opts.tags()));

      JobDetail job = JobBuilder.newJob(DispatchCommandJob.class)
          .withIdentity(jobKey)
          .usingJobData(jdm)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKeyFor(jobKey))
          .startAt(Date.from(whenUtc))
          .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
          .build();

      scheduler.scheduleJob(job, trigger);
      return id;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String scheduleAfter(MessageEnvelope<? extends Command> cmd, Duration delay, ScheduleOptions opts) {
    return scheduleAt(cmd, Instant.now().plus(delay), opts);
  }

  @Override
  public String scheduleCron(MessageEnvelope<? extends Command> cmd, String cronExpr, ZoneId tz, ScheduleOptions opts) {
    try {
      var id = UUID.randomUUID().toString();
      var jobKey = jobKey(id, cmd);

      var jdm = new JobDataMap();
      jdm.put(DispatchCommandJob.CMD_JSON, mapper.writeValueAsString(cmd));
      jdm.put(DispatchCommandJob.TENANT, cmd.tenantId());
      jdm.put(DispatchCommandJob.IDEM_KEY, Optional.ofNullable(opts.idempotencyKey()).orElse(null));
      jdm.put(DispatchCommandJob.TAGS, mapper.writeValueAsString(opts.tags()));

      JobDetail job = JobBuilder.newJob(DispatchCommandJob.class)
          .withIdentity(jobKey)
          .usingJobData(jdm)
          .build();

      CronScheduleBuilder schedule = CronScheduleBuilder
          .cronSchedule(cronExpr)
          .inTimeZone(TimeZone.getTimeZone(tz));

      switch (opts.misfirePolicy()) {
        case FIRE_NOW -> schedule = schedule.withMisfireHandlingInstructionFireAndProceed();
        case IGNORE -> schedule = schedule.withMisfireHandlingInstructionDoNothing();
        case NEXT_WITH_EXISTING_COUNT -> schedule = schedule.withMisfireHandlingInstructionIgnoreMisfires();
      }

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKeyFor(jobKey))
          .withSchedule(schedule)
          .build();

      scheduler.scheduleJob(job, trigger);
      return id;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean cancel(String scheduleId) {
    try {
      return scheduler.deleteJob(jobKeyById(scheduleId));
    } catch (SchedulerException e) {
      return false;
    }
  }

  @Override
  public boolean reschedule(String scheduleId, Instant newWhenUtc) {
    try {
      var jk = jobKeyById(scheduleId);
      var tk = triggerKeyFor(jk);

      Trigger newTrig = TriggerBuilder.newTrigger()
          .withIdentity(tk)
          .startAt(Date.from(newWhenUtc))
          .withSchedule(SimpleScheduleBuilder.simpleSchedule())
          .build();

      return scheduler.rescheduleJob(tk, newTrig) != null;
    } catch (SchedulerException e) {
      return false;
    }
  }

  @Override
  public Optional<ScheduledInfo> get(String id) {
    try {
      var jk = jobKeyById(id);
      Trigger trig = scheduler.getTrigger(triggerKeyFor(jk));
      return Optional.ofNullable(trig).map(t -> {
        String state;
        try {
          state = scheduler.getTriggerState(t.getKey()).name();
        } catch (SchedulerException se) {
          state = "UNKNOWN";
        }
        return new ScheduledInfo(
            id,
            t.getNextFireTime() == null ? null : t.getNextFireTime().toInstant(),
            state,
            Map.of()
        );
      });
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // ======= Helpers =======

  private static JobKey jobKey(String id, MessageEnvelope<? extends Command> cmd) {
    return new JobKey(id, cmd.payload().getClass().getSimpleName()); // agrupa por tipo de comando
  }

  private static TriggerKey triggerKeyFor(JobKey jobKey) {
    return new TriggerKey(jobKey.getName(), jobKey.getGroup());
  }

  private static JobKey jobKeyById(String id) {
    return new JobKey(id);
  }
}

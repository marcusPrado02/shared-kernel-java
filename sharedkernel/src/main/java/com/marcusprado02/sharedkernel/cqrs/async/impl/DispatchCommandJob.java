package com.marcusprado02.sharedkernel.cqrs.async.impl;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.adapters.out.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandBus;
import com.marcusprado02.sharedkernel.cqrs.command.idempotency.IdKey;
import com.marcusprado02.sharedkernel.cqrs.command.idempotency.IdempotencyStore;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@DisallowConcurrentExecution
public class DispatchCommandJob implements Job {
  public static final String CMD_JSON = "cmdJson";          // Jackson
  public static final String IDEM_KEY = "idempotencyKey";   // opcional
  public static final String TENANT   = "tenantId";
  public static final String TAGS     = "tags";             // JSON (Map<String,String>)

  private final CommandBus commandBus;
  private final ObjectMapper mapper;
  private final IdempotencyStore idem;
  private final MeterRegistry meter;
  private final Tracer tracer;

  public DispatchCommandJob(CommandBus bus, ObjectMapper mapper, IdempotencyStore idem, MeterRegistry meter, Tracer tracer) {
    this.commandBus = bus; this.mapper = mapper; this.idem = idem; this.meter = meter; this.tracer = tracer;
  }

  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    var data    = ctx.getMergedJobDataMap();
    var cmdJson = data.getString(CMD_JSON);
    var tenant  = data.getString(TENANT);
    var idemKey = Optional.ofNullable(data.getString(IDEM_KEY)).orElse("sched:"+ctx.getJobDetail().getKey());

    // TAGS foram gravadas como JSON no scheduler — desserialize aqui
    Map<String,String> tagMap = Map.of();
    try {
      var tagsJson = data.getString(TAGS);
      if (tagsJson != null && !tagsJson.isBlank()) {
        tagMap = mapper.readValue(tagsJson, new TypeReference<Map<String,String>>() {});
      }
    } catch (Exception ignore) {
      // se falhar a leitura de tags, segue sem elas
    }
    var micrometerTags = toTags(tagMap);

    // OpenTelemetry: Span não é AutoCloseable — use Scope dentro do try, e finalize o span no finally
    Span span = tracer.spanBuilder("Scheduler.Dispatch")
        .setAttribute("sched.jobKey", ctx.getJobDetail().getKey().toString())
        .setAttribute("tenant", tenant == null ? "unknown" : tenant)
        .startSpan();

    try (Scope scope = span.makeCurrent()) {
      // Idempotência (dedupe de disparo)
      var claimed = idem.tryClaim(new IdKey(tenant, "scheduler.dispatch", idemKey), Duration.ofHours(1), ctx.getFireInstanceId());
      if (!claimed) {
        meter.counter("sched.duplicate", micrometerTags).increment();
        return;
      }

      // Envelope do comando
      MessageEnvelope<? extends Command> env = mapper.readValue(cmdJson, MessageEnvelope.class);

      // Dispara no CommandBus (behaviors aplicam tx/outbox/audit/tracing)
      commandBus.dispatch(env);

      idem.confirm(new IdKey(tenant, "scheduler.dispatch", idemKey));
      meter.counter("sched.dispatched", micrometerTags).increment();

    } catch (Exception ex) {
      meter.counter("sched.error", micrometerTags).increment();
      // Deixe o Quartz decidir re-execução via política
      throw new JobExecutionException(ex, shouldRefire(ctx));
    } finally {
      span.end();
    }
  }

  private static Iterable<Tag> toTags(Map<String,String> m) {
    if (m == null || m.isEmpty()) return Tags.empty();
    var arr = new String[m.size() * 2];
    int i = 0;
    for (var e : m.entrySet()) {
      arr[i++] = e.getKey();
      arr[i++] = e.getValue();
    }
    return Tags.of(arr);
  }

  private boolean shouldRefire(JobExecutionContext ctx) {
    // Personalize conforme política: pode ler contexto/trigger e decidir se refire
    return true;
  }
}

package com.marcusprado02.sharedkernel.crosscutting.hook;


import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class HookBus {
    private final Map<String, CopyOnWriteArrayList<HookRegistration<?>>> map = new ConcurrentHashMap<>();
    private final ExecutorService vt = Executors.newVirtualThreadPerTaskExecutor();
    private final Telemetry telemetry;

    public HookBus(Telemetry telemetry){ this.telemetry = telemetry==null?Telemetry.NOOP:telemetry; }

    public <E> AutoCloseable register(HookRegistration<E> reg){
        map.computeIfAbsent(key(reg.topic(), reg.phase()), k -> new CopyOnWriteArrayList<>()).add(reg);
        map.get(key(reg.topic(), reg.phase())).sort(Comparator.comparingInt(r -> ((HookRegistration<?>)r).priority()));
        return () -> map.getOrDefault(key(reg.topic(), reg.phase()), new CopyOnWriteArrayList<>()).remove(reg);
    }

    public <E> List<HookResult> fire(String topic, HookPhase phase, E event, HookContext baseCtx){
        var ctx = HookContext.builder()
            .topic(topic).phase(phase.name().toLowerCase())
            .clock(baseCtx.clock()).cancellation(baseCtx.cancellation())
            .build();

        var regs = map.getOrDefault(key(topic, phase), new CopyOnWriteArrayList<>())
            .stream().map(r -> (HookRegistration<E>) r).toList();

        var results = new ArrayList<HookResult>(regs.size());
        for (var reg : regs) {
            if (ctx.cancellation().isCancelled()) break;
            if (reg.filter()!=null && !reg.filter().test(event)) continue;

            var start = Instant.now(ctx.clock());
            try {
                executeWithPolicy(reg, event, ctx);
                var dur = Duration.between(start, Instant.now(ctx.clock()));
                telemetry.count("hook.success", 1, Map.of("topic",topic,"phase",phase.name(),"handler",reg.handler().getClass().getSimpleName()));
                results.add(new HookResult(true, dur, Optional.empty()));
            } catch (Exception ex) {
                var dur = Duration.between(start, Instant.now(ctx.clock()));
                telemetry.count("hook.error", 1, Map.of("topic",topic,"phase",phase.name(),"handler",reg.handler().getClass().getSimpleName()));
                results.add(new HookResult(false, dur, Optional.of(ex)));
                if (!reg.policy().swallowErrors()) throw new HookRuntimeException("Hook failed", ex);
            }
        }
        return results;
    }

    private <E> void executeWithPolicy(HookRegistration<E> reg, E event, HookContext ctx) throws Exception {
        var policy = reg.policy();
        if (!policy.rateLimiter().tryAcquire()) throw new HookRuntimeException("rate-limit");
        Callable<Void> task = () -> { reg.handler().handle(event, ctx); return null; };

        Callable<Void> timed = () -> {
            Future<Void> f = (policy.isolate() ? vt.submit(task) : Executors.newSingleThreadExecutor().submit(task));
            try { return f.get(policy.timeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) { f.cancel(true); throw te; }
        };

        int attempt = 0;
        while (true) {
            try { policy.circuitBreaker().call(timed); return; }
            catch (Exception e) {
                attempt++;
                if (attempt >= policy.maxAttempts()) throw e;
                long backoff = Math.min(policy.maxBackoff().toMillis(),
                                        (long)(policy.baseBackoff().toMillis() * Math.pow(2, attempt-1)));
                Thread.sleep(ThreadLocalRandom.current().nextLong(backoff));
            }
        }
    }

    private static String key(String topic, HookPhase phase){ return topic + "|" + phase.name(); }
    public void shutdown(){ vt.shutdown(); }
}



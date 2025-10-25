package com.marcusprado02.sharedkernel.observability.metrics.collectors.impl;

import javax.management.*;
import javax.management.openmbean.CompositeData;

import com.marcusprado02.sharedkernel.observability.metrics.collectors.*;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import java.lang.management.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

public final class GcMonitorMXBean implements GarbageCollectorMonitor {

    private final ScheduledExecutorService ses;
    private final List<GcListener> listeners = new CopyOnWriteArrayList<>();
    private final int windowSeconds;

    // janelas por “ring buffer”
    private final LongAdder pauseCount = new LongAdder();
    private final DoubleAdder pauseSumMs = new DoubleAdder();
    private final ConcurrentLinkedQueue<Long> pauseWindowMs = new ConcurrentLinkedQueue<>();

    private final LongAdder youngCount = new LongAdder();
    private final LongAdder fullCount  = new LongAdder();
    private final LongAdder concCount  = new LongAdder();

    private final LongAdder gcTimeNanos = new LongAdder();
    private final LongAdder wallTimeNanos = new LongAdder();

    // taxas
    private final AtomicLong lastTotalAlloc = new AtomicLong(0); // via MemoryPool delta
    private final AtomicLong lastOldUsed    = new AtomicLong(0);
    private final DoubleAdder allocBps      = new DoubleAdder();
    private final DoubleAdder promoBps      = new DoubleAdder();

    // ocupações correntes
    private volatile long edenUsed, survivorUsed, oldUsed, metaspaceUsed;
    private volatile double oldUtilizationPct;

    private final List<ListenerBinding> bindings = new ArrayList<>();

    public GcMonitorMXBean(int windowSeconds) {
        this.windowSeconds = windowSeconds;
        this.ses = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "gc-monitor"); t.setDaemon(true); return t;
        });
        bindToGcNotifications();
        // sample ticking para janelar valores e limpar velhos
        ses.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private static class ListenerBinding {
        final NotificationEmitter emitter; final NotificationListener listener;
        ListenerBinding(NotificationEmitter e, NotificationListener l){ this.emitter=e; this.listener=l; }
        void remove(){ try { emitter.removeNotificationListener(listener); } catch (Exception ignore) {} }
    }

    @SuppressWarnings("unchecked")
    private void bindToGcNotifications() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gc instanceof NotificationEmitter ne) {
                NotificationListener nl = (n, hb) -> {
                    if (!GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(n.getType())) return;
                    var info = GarbageCollectionNotificationInfo.from((CompositeData) n.getUserData());
                    handleGc(info, gc.getName());
                };
                ne.addNotificationListener(nl, null, null);
                bindings.add(new ListenerBinding(ne, nl));
            }
        }
    }

    private void handleGc(GarbageCollectionNotificationInfo info, String gcName) {
        GcInfo gi = info.getGcInfo();
        long pauseNanos = TimeUnit.MILLISECONDS.toNanos(info.getGcInfo().getDuration());
        var phase = mapPhase(info.getGcAction(), info.getGcCause());

        Map<String, PoolDelta> pools = new LinkedHashMap<>();
        var before = gi.getMemoryUsageBeforeGc();
        var after  = gi.getMemoryUsageAfterGc();
        for (var e : before.entrySet()) {
            var pool = e.getKey();
            var b = e.getValue(); var a = after.get(pool);
            if (a != null) {
                pools.put(pool, new PoolDelta(pool, b.getUsed(), a.getUsed(), b.getCommitted(), a.getCommitted()));
            }
        }

        // manter ocupações correntes por pool
        edenUsed      = usageOf(after, "Eden", "PS Eden Space", "G1 Eden Space", "ZGC Eden Space", "Shenandoah Eden");
        survivorUsed  = usageOf(after, "Survivor", "PS Survivor Space", "G1 Survivor Space", "ZGC Survivor Space", "Shenandoah Survivor");
        oldUsed       = usageOf(after, "Old Gen", "Tenured Gen", "PS Old Gen", "G1 Old Gen", "ZGC Old", "Shenandoah");
        metaspaceUsed = usageOf(after, "Metaspace", "Metaspace");

        long oldCommitted = committedOf(after, "PS Old Gen", "G1 Old Gen", "ZGC Old", "Tenured Gen", "Shenandoah");
        oldUtilizationPct = oldCommitted > 0 ? (oldUsed * 100.0 / oldCommitted) : 0.0;

        // contadores por fase
        switch (phase) {
            case YOUNG, MIXED -> youngCount.increment();
            case FULL -> fullCount.increment();
            case CONCURRENT -> concCount.increment();
            default -> {}
        }

        // janelas de pausa
        pauseWindowMs.add(TimeUnit.NANOSECONDS.toMillis(pauseNanos));
        pauseCount.increment();
        pauseSumMs.add(TimeUnit.NANOSECONDS.toMillis(pauseNanos));
        gcTimeNanos.add(pauseNanos);

        // promoção ≈ redução do young/eden e aumento do old (heurística)
        long beforeOld = before.values().stream().mapToLong(mu -> mu.getUsed()).sum();
        long afterOld  = after.values().stream().mapToLong(mu -> mu.getUsed()).sum();
        long promo = Math.max(0, afterOld - beforeOld); // simplista; GC específico pode melhorar
        promoBps.add(promo); // acumulado; normalizamos no tick

        // total alloc: usamos delta de “heap used + GC freed” ⇒ aproximado por (eden before - eden after) + promo
        long freed = Math.max(0, edenBefore(before) - edenAfter(after));
        long alloc = Math.max(0, freed + promo);
        allocBps.add(alloc);

        var evt = new GcEvent(Instant.ofEpochMilli(gi.getStartTime()), gcName, info.getGcAction(), info.getGcCause(), phase, pauseNanos, pools);
        var snap = current(); // após atualizar janelas
        for (var l : listeners) try { l.onGc(evt, snap); } catch (Throwable ignore) {}
    }

    private static long usageOf(Map<String, MemoryUsage> m, String... candidates){
        for (String c : candidates) {
            for (var e : m.entrySet()) if (e.getKey().contains(c)) return e.getValue().getUsed();
        }
        return 0;
    }
    private static long committedOf(Map<String, MemoryUsage> m, String... candidates){
        for (String c : candidates) {
            for (var e : m.entrySet()) if (e.getKey().contains(c)) return e.getValue().getCommitted();
        }
        return 0;
    }
    private static long edenBefore(Map<String, MemoryUsage> m){
        for (var k : List.of("Eden","PS Eden Space","G1 Eden Space","ZGC Eden Space","Shenandoah Eden")){
            for (var e : m.entrySet()) if (e.getKey().contains(k)) return e.getValue().getUsed();
        }
        return 0;
    }
    private static long edenAfter(Map<String, MemoryUsage> m){ return edenBefore(m); }

    private GcPhase mapPhase(String action, String cause) {
        var a = (action==null?"":action.toLowerCase());
        if (a.contains("minor") || a.contains("young")) return GcPhase.YOUNG;
        if (a.contains("mixed")) return GcPhase.MIXED;
        if (a.contains("major") || a.contains("full")) return GcPhase.FULL;
        if (a.contains("concurrent")) return GcPhase.CONCURRENT;
        if (a.contains("metadata")) return GcPhase.META;
        return GcPhase.OTHER;
    }

    private void tick() {
        wallTimeNanos.add(TimeUnit.SECONDS.toNanos(1));

        // manter janela de N segundos para percentis
        while (pauseWindowMs.size() > windowSeconds) pauseWindowMs.poll();

        // normaliza taxas (B/s) usando acumuladores
        // aqui aplicamos EMA leve para suavizar (alpha=0.3)
        ema(allocBps, lastTotalAlloc);
        ema(promoBps, lastOldUsed);
    }

    private static void ema(DoubleAdder adder, AtomicLong state){
        double val = adder.sumThenReset(); // bytes desta 1s janela
        long prev = state.get();
        long next = Math.round(prev==0 ? val : (0.7*prev + 0.3*val));
        state.set(next);
    }

    @Override public void addListener(GcListener l) { listeners.add(l); }

    @Override public GcSnapshot current() {
        var arr = pauseWindowMs.stream().mapToLong(Long::longValue).toArray();
        Arrays.sort(arr);
        double p50 = percentile(arr, 50), p95 = percentile(arr, 95), p99 = percentile(arr, 99);
        long wall = wallTimeNanos.sum();
        long gc   = gcTimeNanos.sum();
        double ratio = wall>0 ? (gc * 100.0 / wall) : 0.0;

        return new GcSnapshot(
                p50, p95, p99,
                ratio,
                lastTotalAlloc.get(), lastOldUsed.get(),
                edenUsed, survivorUsed, oldUsed, metaspaceUsed,
                oldUtilizationPct,
                youngCount.sum(), fullCount.sum(), concCount.sum(),
                windowSeconds
        );
    }

    private static double percentile(long[] sortedMs, int p){
        if (sortedMs.length==0) return 0;
        double rank = (p/100.0) * (sortedMs.length - 1);
        int lo = (int)Math.floor(rank), hi = (int)Math.ceil(rank);
        if (lo==hi) return sortedMs[lo];
        return sortedMs[lo] + (rank-lo) * (sortedMs[hi]-sortedMs[lo]);
    }

    @Override public void close() {
        for (var b : bindings) b.remove();
        ses.shutdownNow();
    }
}

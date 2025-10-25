package com.marcusprado02.sharedkernel.observability.metrics.collectors;

public record GcSnapshot(
        // pausas
        double pauseP50ms, double pauseP95ms, double pauseP99ms,
        // % tempo gasto em GC na janela
        double gcTimeRatioPct,
        // taxas
        double allocationBytesPerSec, double promotionBytesPerSec,
        // ocupações
        long edenUsed, long survivorUsed, long oldUsed, long metaspaceUsed,
        double oldUtilizationPct,
        // contagens por fase na janela
        long youngCount, long fullCount, long concurrentCount,
        // últimos N segundos analisados
        int windowSeconds
){}
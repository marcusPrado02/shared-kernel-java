package com.marcusprado02.sharedkernel.cqrs.bulk;


import java.time.Duration;

public record BulkPolicy(
        int chunkSize,                 // tamanho do bloco
        int maxConcurrency,            // paralelismo de itens dentro do chunk
        FailureMode failureMode,       // estratégia de falha
        Integer stopOnFailures,        // para STOP_ON_THRESHOLD
        boolean ordered,               // respeitar ordem de entrada
        Duration interChunkDelay,      // atraso entre chunks (throttle)
        boolean perItemIdempotency,    // reusa idempotencyKey por item
        boolean captureItemEnvelope    // agrega metadados por item no resultado
) {
    public enum FailureMode { FAIL_FAST, CONTINUE_ON_ERROR, STOP_ON_THRESHOLD }
    public static BulkPolicy balanced() {
        return new BulkPolicy(200, Math.max(2, Runtime.getRuntime().availableProcessors()/2),
                FailureMode.CONTINUE_ON_ERROR, null, true, Duration.ZERO, true, true);
    }
}

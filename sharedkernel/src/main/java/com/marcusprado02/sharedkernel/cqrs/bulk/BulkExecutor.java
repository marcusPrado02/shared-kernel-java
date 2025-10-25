package com.marcusprado02.sharedkernel.cqrs.bulk;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.marcusprado02.sharedkernel.cqrs.bulk.spi.ParallelismExecutor;
import com.marcusprado02.sharedkernel.cqrs.bulk.spi.RateLimiter;
import com.marcusprado02.sharedkernel.cqrs.command.*;

/** Orquestra a execução em chunks e a agregação do resultado. */
public final class BulkExecutor {
    private final CommandBus bus;
    private final ParallelismExecutor executor;
    private final RateLimiter rateLimiter;
    private final BulkErrorClassifier errorClassifier;

    public BulkExecutor(CommandBus bus, ParallelismExecutor executor, RateLimiter rl, BulkErrorClassifier err) {
        this.bus = bus; this.executor = executor; this.rateLimiter = rl; this.errorClassifier = err;
    }

    public <R> CompletionStage<BulkResult<R>> execute(BulkCommand<R> bulk, Consumer<CommandMetadata.Builder> metaCustomizer) {
        var items = switch (bulk) {
            case HomogeneousBulkCommand<?,?> h -> (List<Command<R>>) (List<?>) h.items();
            case HeterogeneousBulkCommand<R>  h -> h.items();
        };
        var policy = switch (bulk) {
            case HomogeneousBulkCommand<?,?> h -> h.policy();
            case HeterogeneousBulkCommand<R>  h -> h.policy();
        };

        var started = System.nanoTime();
        List<BulkResult.ItemResult<R>> results = Collections.synchronizedList(new ArrayList<>(items.size()));
        var chunks = chunkIndices(items.size(), policy.chunkSize());

        CompletableFuture<BulkResult<R>> done = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                int failuresSoFar = 0;
                for (var range : chunks) {
                    // throttle por chunk
                    rateLimiter.acquire();

                    var futures = new ArrayList<CompletableFuture<Void>>();
                    var sem = new Semaphore(Math.max(1, policy.maxConcurrency()));

                    for (int i = range.start; i < range.end; i++) {
                        final int index = i;
                        sem.acquire();
                        var f = dispatchOne(items.get(index), index, policy, metaCustomizer)
                                .whenComplete((r, t) -> sem.release())
                                .thenAccept(results::add);
                        futures.add(f);
                    }
                    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

                    // políticas de parada
                    failuresSoFar += (int) results.stream()
                            .filter(ir -> ir.index() >= range.start && ir.index() < range.end)
                            .filter(ir -> ir.status() == BulkResult.ItemResult.Status.FAILED || ir.status() == BulkResult.ItemResult.Status.REJECTED)
                            .count();

                    if (policy.failureMode() == BulkPolicy.FailureMode.FAIL_FAST && failuresSoFar > 0) break;
                    if (policy.failureMode() == BulkPolicy.FailureMode.STOP_ON_THRESHOLD &&
                        policy.stopOnFailures() != null && failuresSoFar >= policy.stopOnFailures()) break;

                    if (!policy.interChunkDelay().isZero()) Thread.sleep(policy.interChunkDelay().toMillis());
                }

                results.sort(Comparator.comparingInt(BulkResult.ItemResult::index));
                var elapsed = Duration.ofNanos(System.nanoTime()-started);
                var succeeded = (int) results.stream().filter(r -> r.status() == BulkResult.ItemResult.Status.COMPLETED).count();
                var failed = (int) results.stream().filter(r -> r.status() == BulkResult.ItemResult.Status.FAILED || r.status() == BulkResult.ItemResult.Status.REJECTED).count();
                done.complete(new BulkResult<>(items.size(), succeeded, failed, elapsed, List.copyOf(results)));
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        });
        return done;
    }

    private <R> CompletableFuture<BulkResult.ItemResult<R>> dispatchOne(Command<R> cmd, int index, BulkPolicy policy, Consumer<CommandMetadata.Builder> metaCustomizer) {
        var start = System.nanoTime();
        // idempotência por item: derive uma chave a partir de index e do comando
        Consumer<CommandMetadata.Builder> meta = b -> {
            metaCustomizer.accept(b);
            if (policy.perItemIdempotency()) {
                b.idempotencyKey(UUID.nameUUIDFromBytes((cmd.getClass().getName() + "#" + index).getBytes()).toString());
            }
        };
        return bus.dispatch(cmd, meta).toCompletableFuture().handle((res, err) -> {
            long took = Duration.ofNanos(System.nanoTime() - start).toMillis();
            if (err != null) {
                var mapped = errorClassifier.map((Throwable) err);
                var status = mapped.reject() ? BulkResult.ItemResult.Status.REJECTED : BulkResult.ItemResult.Status.FAILED;
                return new BulkResult.ItemResult<R>(index, guessItemId(cmd), status, Optional.empty(), Optional.of(err), took);
            }
            return switch (res.status()) {
                case COMPLETED -> new BulkResult.ItemResult<R>(index, guessItemId(cmd), BulkResult.ItemResult.Status.COMPLETED, res.value(), Optional.empty(), took);
                case REJECTED  -> new BulkResult.ItemResult<R>(index, guessItemId(cmd), BulkResult.ItemResult.Status.REJECTED, Optional.empty(), res.error(), took);
                case FAILED, RETRY_SCHEDULED, ACCEPTED -> new BulkResult.ItemResult<R>(index, guessItemId(cmd), BulkResult.ItemResult.Status.SKIPPED, Optional.empty(), res.error(), took);
            };
        });
    }

    private static String guessItemId(Command<?> c) {
        try {
            var m = c.getClass().getDeclaredMethod("orderId");
            m.setAccessible(true);
            var v = m.invoke(c);
            return v == null ? "" : v.toString();
        } catch (Exception ignore) { return ""; }
    }

    private static List<Range> chunkIndices(int total, int size) {
        if (total <= 0) return List.of();
        int s = Math.max(1, size);
        var list = new ArrayList<Range>((total + s - 1) / s);
        for (int i = 0; i < total; i += s) list.add(new Range(i, Math.min(total, i + s)));
        return list;
    }
    private record Range(int start, int end) {}
}

package com.marcusprado02.sharedkernel.infrastructure.blob.adapter;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.blob.BlobId;
import com.marcusprado02.sharedkernel.infrastructure.blob.BlobReadRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.BlobStorageAdapter;
import com.marcusprado02.sharedkernel.infrastructure.blob.BlobWriteRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.DownloadResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.StatResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.UploadResult;

public abstract class BaseBlobStorageAdapter implements BlobStorageAdapter {

    protected final Tracer tracer;
    protected final MeterRegistry meter;
    protected final Retry retry;
    protected final CircuitBreaker cb;

    protected BaseBlobStorageAdapter(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb) {
        this.tracer = tracer;
        this.meter = meter;
        this.retry = retry;
        this.cb = cb;
    }

    @Override
    public UploadResult putObject(BlobWriteRequest req) {
        var span = tracer.spanBuilder("blob.put").setAttribute("bucket", req.id().bucket()).setAttribute("key", req.id().key()).startSpan();
        long start = System.nanoTime();
        try (var scope = span.makeCurrent()) {
            UploadResult r = CircuitBreaker.decorateSupplier(cb,
                    Retry.decorateSupplier(retry, () -> doPut(req))
            ).get();
            meter.counter("blob.put.count", "backend", backendName()).increment();
            meter.timer("blob.put.latency", "backend", backendName()).record(Duration.ofNanos(System.nanoTime() - start));
            return r;
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR, e.getMessage());
            meter.counter("blob.put.errors", "backend", backendName()).increment();
            throw e;
        } finally { span.end(); }
    }

    @Override
    public DownloadResult getObject(BlobReadRequest req) {
        var span = tracer.spanBuilder("blob.get").setAttribute("bucket", req.id().bucket()).setAttribute("key", req.id().key()).startSpan();
        long start = System.nanoTime();
        try (var scope = span.makeCurrent()) {
            DownloadResult r = CircuitBreaker.decorateSupplier(cb,
                    Retry.decorateSupplier(retry, () -> doGet(req))
            ).get();
            meter.counter("blob.get.count", "backend", backendName()).increment();
            meter.timer("blob.get.latency", "backend", backendName()).record(Duration.ofNanos(System.nanoTime() - start));
            return r;
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR, e.getMessage());
            meter.counter("blob.get.errors", "backend", backendName()).increment();
            throw e;
        } finally { span.end(); }
    }

    @Override
    public void getObjectToFile(BlobReadRequest req, Path target) {
        try (var dr = getObject(req)) {
            java.nio.file.Files.copy(dr.stream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override public CompletableFuture<UploadResult> putObjectAsync(BlobWriteRequest req){ return CompletableFuture.supplyAsync(() -> putObject(req)); }
    @Override public CompletableFuture<DownloadResult> getObjectAsync(BlobReadRequest req){ return CompletableFuture.supplyAsync(() -> getObject(req)); }
    @Override public CompletableFuture<Void> getObjectToFileAsync(BlobReadRequest req, Path target){ return CompletableFuture.runAsync(() -> getObjectToFile(req, target)); }
    @Override public CompletableFuture<StatResult> statAsync(BlobId id){ return CompletableFuture.supplyAsync(() -> stat(id)); }

    // Backends devem implementar:
    protected abstract UploadResult doPut(BlobWriteRequest req);
    protected abstract DownloadResult doGet(BlobReadRequest req);
}


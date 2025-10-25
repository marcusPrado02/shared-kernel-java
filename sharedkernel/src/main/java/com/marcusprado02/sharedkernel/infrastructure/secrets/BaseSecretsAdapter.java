package com.marcusprado02.sharedkernel.infrastructure.secrets;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope; // <— IMPORT NECESSÁRIO
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class BaseSecretsAdapter implements SecretsAdapter {

    protected final Tracer tracer;
    protected final MeterRegistry meter;
    protected final Retry retry;
    protected final CircuitBreaker cb;
    protected final SecretsCache cache; // pluggable

    protected BaseSecretsAdapter(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb, SecretsCache cache) {
        this.tracer = tracer; this.meter = meter; this.retry = retry; this.cb = cb; this.cache = cache;
    }

    @Override
    public SecretValue getSecret(SecretRef ref, ReadOptions opts) {
        final String key = cacheKey(ref, opts);
        // ReadOptions atual tem requestFresh(): quando true, BYPASS cache
        if (cache != null && !opts.requestFresh()) {
            Optional<SecretValue> cached = cache.get(key);
            if (cached.isPresent()) return cached.get();
        }

        var span = tracer.spanBuilder("secrets.get")
                .setAttribute("secrets.backend", backendName())
                .setAttribute("secrets.path", ref.fqn())
                .startSpan();
        long start = System.nanoTime();

        try (Scope ignored = span.makeCurrent()) {
            SecretValue value = CircuitBreaker.decorateSupplier(cb,
                    Retry.decorateSupplier(retry, () -> doGet(ref, opts))
            ).get();
            meter.counter("secrets.get.count", "backend", backendName()).increment();
            meter.timer("secrets.get.latency", "backend", backendName())
                 .record(Duration.ofNanos(System.nanoTime() - start));
            if (cache != null && !opts.requestFresh()) cache.put(key, value, cacheTtl(ref));
            return value;
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR);
            meter.counter("secrets.get.errors", "backend", backendName()).increment();
            throw e;
        } finally { span.end(); }
    }

    @Override
    public CompletableFuture<SecretValue> getSecretAsync(SecretRef ref, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getSecret(ref, opts));
    }

    @Override
    public void putSecret(SecretRef ref, SecretValue value, WriteOptions opts) {
        var span = tracer.spanBuilder("secrets.put")
                .setAttribute("secrets.backend", backendName())
                .setAttribute("secrets.path", ref.fqn())
                .startSpan();
        long start = System.nanoTime();
        try (Scope ignored = span.makeCurrent()) {
            CircuitBreaker.decorateRunnable(cb,
                    Retry.decorateRunnable(retry, () -> doPut(ref, value, opts))
            ).run();
            meter.counter("secrets.put.count", "backend", backendName()).increment();
            meter.timer("secrets.put.latency", "backend", backendName())
                 .record(Duration.ofNanos(System.nanoTime() - start));
            if (cache != null) cache.invalidate(prefixKey(ref));
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR);
            meter.counter("secrets.put.errors", "backend", backendName()).increment();
            throw e;
        } finally { span.end(); }
    }

    @Override public CompletableFuture<Void> putSecretAsync(SecretRef ref, SecretValue value, WriteOptions opts) {
        return CompletableFuture.runAsync(() -> putSecret(ref, value, opts));
    }

    // Ajuste da cacheKey para não depender de minVersionId() (que não existe no ReadOptions atual)
    protected String cacheKey(SecretRef ref, ReadOptions opts){
        // Se quiser incluir algum aspecto de “versão” no cache, derive de ref.labels() ou opts.maxStaleness()
        return backendName() + "|" + ref.fqn();
    }

    protected String prefixKey(SecretRef ref){ return backendName()+"|"+ref.fqn(); }
    protected Duration cacheTtl(SecretRef ref){ return Duration.ofSeconds(60); }

    protected abstract SecretValue doGet(SecretRef ref, ReadOptions opts);
    protected abstract void doPut(SecretRef ref, SecretValue value, WriteOptions opts);
    protected abstract void doDelete(SecretRef ref, boolean deleteVersions);
    protected abstract SecretMetadata doStat(SecretRef ref);
    protected abstract void doEnableVersion(SecretRef ref, String versionId, boolean enabled);
    protected abstract String doRotate(SecretRef ref, RotationPolicy policy);

    // Métodos default encaminhando p/ abstratos:
    @Override public SecretMetadata stat(SecretRef ref){ return doStat(ref); }
    @Override public void deleteSecret(SecretRef ref, boolean deleteVersions){ doDelete(ref, deleteVersions); }
    @Override public void enableVersion(SecretRef ref, String versionId, boolean enabled){ doEnableVersion(ref, versionId, enabled); }
    @Override public String rotate(SecretRef ref, RotationPolicy policy){ return doRotate(ref, policy); }
    @Override public List<String> list(SecretRef prefix){ return doList(prefix); }
    protected List<String> doList(SecretRef prefix){ throw new UnsupportedOperationException(); }

    // Helpers para spans/metrics nas operações SecretId-tipadas
    protected <T> T withSpan(String op, SecretId id, java.util.function.Supplier<T> supplier) {
        var span = tracer.spanBuilder("secrets."+op)
                .setAttribute("secrets.backend", backendName())
                .setAttribute("secrets.namespace", id.namespace())
                .setAttribute("secrets.name", id.name())
                .startSpan();
        long start = System.nanoTime();
        try (Scope ignored = span.makeCurrent()) {
            T out = CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, supplier)).get();
            meter.counter("secrets."+op+".count", "backend", backendName()).increment();
            meter.timer("secrets."+op+".latency", "backend", backendName())
                 .record(Duration.ofNanos(System.nanoTime() - start));
            return out;
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR, e.getMessage());
            meter.counter("secrets."+op+".errors", "backend", backendName()).increment();
            throw e;
        } finally { span.end(); }
    }

    @Override public CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions o){ return CompletableFuture.supplyAsync(() -> getString(id,o)); }
    @Override public CompletableFuture<Secret<Map<String,Object>>> getJsonAsync(SecretId id, ReadOptions o){ return CompletableFuture.supplyAsync(() -> getJson(id,o)); }
    @Override public CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions o){ return CompletableFuture.supplyAsync(() -> getBinary(id,o)); }
}

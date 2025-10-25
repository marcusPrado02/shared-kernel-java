package com.marcusprado02.sharedkernel.infrastructure.secrets.adapter.env;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.marcusprado02.sharedkernel.infrastructure.secrets.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;

public class EnvSecretsAdapter extends BaseSecretsAdapter {

    public EnvSecretsAdapter(Tracer t, MeterRegistry m, Retry r, CircuitBreaker c, SecretsCache cache) {
        super(t, m, r, c, cache);
    }

    /* ===================== Helpers ===================== */

    private static String envKey(SecretId id) {
        // CONVENÇÃO: NAMESPACE_NAME -> UPPER_SNAKE sem chars fora de [A-Z0-9_]
        String key = (id.namespace() + "_" + id.name())
                .toUpperCase()
                .replaceAll("[^A-Z0-9_]", "_");
        return key;
    }

    private static String envKey(SecretRef ref) {
        // ref -> SecretId (environment vira namespace; path vira name)
        String ns = Optional.ofNullable(ref.environment()).orElse("/");
        return envKey(SecretId.of(ns, ref.path()));
    }

    /* ========== API tipada (SecretId) ========== */

    @Override
    public Secret<String> getString(SecretId id, ReadOptions o) {
        return withSpan("getString", id, () -> {
            String key = envKey(id);
            String v = System.getenv(key);
            if (v == null && o.failIfMissing()) {
                throw new IllegalArgumentException("ENV missing: " + key);
            }
            return new Secret<>(id, SecretType.STRING, v, "env", "current",
                    Instant.now(), null, Map.of(), "env");
        });
    }

    @Override
    public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions o) {
        var s = getString(id, o);
        if (s.value() == null) {
            return new Secret<>(s.id(), SecretType.JSON, Map.of(),
                    s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        }
        try {
            Map<String, Object> map = JsonMapper.builder().build()
                    .readValue(s.value(), new TypeReference<Map<String, Object>>() {});
            if (!o.fieldsWhitelist().isEmpty()) {
                map.keySet().removeIf(k -> !o.fieldsWhitelist().contains(k));
            }
            return new Secret<>(s.id(), SecretType.JSON, map,
                    s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON in ENV for key " + envKey(id), e);
        }
    }

    @Override
    public Secret<byte[]> getBinary(SecretId id, ReadOptions o) {
        // ENV é texto; retornamos bytes do valor textual (ou vazio)
        var s = getString(id, o);
        byte[] bytes = s.value() == null ? new byte[0] : s.value().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new Secret<>(id, SecretType.BINARY, bytes,
                s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
    }

    @Override public Secret<String> put(PutSecretRequest r) { throw new UnsupportedOperationException("ENV is read-only"); }
    @Override public void delete(SecretId id, boolean all) { throw new UnsupportedOperationException("ENV is read-only"); }
    @Override public List<Secret<String>> listVersions(SecretId id, int page, String c) { return List.of(getString(id, ReadOptions.defaults())); }
    @Override public void promoteStage(SecretId id, String from, String to) { /* no-op */ }
    @Override public Secret<String> rotate(SecretId id, RotationPolicy p, java.util.function.Supplier<Object> g) { throw new UnsupportedOperationException("ENV is read-only"); }
    @Override public ListResult list(String ns, String prefix, int page, String c) { return new ListResult(List.of(), null, false); }

    /* ========== API SecretRef (métodos abstratos do BaseSecretsAdapter) ========== */

    @Override
    protected SecretValue doGet(SecretRef ref, ReadOptions opts) {
        String key = envKey(ref);
        String v = System.getenv(key);
        if (v == null && opts.failIfMissing()) {
            throw new IllegalArgumentException("ENV missing: " + key);
        }
        return new SecretValue(v == null ? null : v.toCharArray(), "text/plain");
    }

    @Override
    protected void doPut(SecretRef ref, SecretValue value, WriteOptions opts) {
        // ENV é somente leitura no runtime — não implementado
        throw new UnsupportedOperationException("ENV is read-only");
    }

    @Override
    protected void doDelete(SecretRef ref, boolean deleteVersions) {
        // ENV é somente leitura
        throw new UnsupportedOperationException("ENV is read-only");
    }

    @Override
    protected SecretMetadata doStat(SecretRef ref) {
        // Metadata mínima
        var id = SecretId.of(Optional.ofNullable(ref.environment()).orElse("/"), ref.path());
        var exists = System.getenv(envKey(ref)) != null;
        var version = "env";
        var created = Instant.EPOCH;
        var versions = List.of(new SecretVersion(version, created, exists, null));
        return new SecretMetadata(ref, backendName(), version, versions, Map.of());
    }

    @Override
    protected void doEnableVersion(SecretRef ref, String versionId, boolean enabled) {
        // Não aplicável
    }

    @Override
    protected String doRotate(SecretRef ref, RotationPolicy policy) {
        // Não aplicável
        throw new UnsupportedOperationException("ENV is read-only");
    }
}

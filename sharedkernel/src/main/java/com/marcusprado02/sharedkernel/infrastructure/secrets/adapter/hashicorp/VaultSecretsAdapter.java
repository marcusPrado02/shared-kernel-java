package com.marcusprado02.sharedkernel.infrastructure.secrets.adapter.hashicorp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.infrastructure.secrets.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;

public class VaultSecretsAdapter extends BaseSecretsAdapter {

    private static final String KV_MOUNT = "secret"; // ajuste se usar outro mount
    private final HttpClient http;
    private final String baseUrl; // ex.: https://vault:8200
    private final String token;   // header X-Vault-Token
    private final ObjectMapper json = new ObjectMapper();

    public VaultSecretsAdapter(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb, SecretsCache cache,
                               String baseUrl, String token) {
        super(tracer, meter, retry, cb, cache);
        this.http = HttpClient.newHttpClient();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
    }

    /* ========================= Helpers ========================= */

    private static String normPath(String ns, String name) {
        String p = (Optional.ofNullable(ns).orElse("") + "/" + Objects.requireNonNull(name)).replaceAll("^/+", "");
        return p;
    }

    private String dataUrl(String path) { return baseUrl + "/v1/" + KV_MOUNT + "/data/" + path; }
    private String metaUrl(String path) { return baseUrl + "/v1/" + KV_MOUNT + "/metadata/" + path; }

    private HttpRequest.Builder req(URI uri) {
        return HttpRequest.newBuilder(uri).header("X-Vault-Token", token);
    }

    private static String str(byte[] b) {
        return new String(b, java.nio.charset.StandardCharsets.UTF_8);
    }

    // Extrai data.data.value (STRING) do body KV v2
    @SuppressWarnings("unchecked")
    private String extractValue(String body) {
        try {
            Map<String, Object> root = json.readValue(body, Map.class);
            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null) return null;
            Map<String, Object> inner = (Map<String, Object>) data.get("data");
            if (inner == null) return null;
            Object v = inner.get("value");
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) { throw new RuntimeException("Vault parse failed", e); }
    }

    // Extrai data.data.value_b64 (BINARY) ou value (fallback string->bytes)
    @SuppressWarnings("unchecked")
    private byte[] extractBinary(String body) {
        try {
            Map<String, Object> root = json.readValue(body, Map.class);
            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null) return null;
            Map<String, Object> inner = (Map<String, Object>) data.get("data");
            if (inner == null) return null;
            Object vb = inner.get("value_b64");
            if (vb instanceof String b64) return Base64.getDecoder().decode(b64);
            Object vs = inner.get("value");
            return vs == null ? null : String.valueOf(vs).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Vault parse failed", e); }
    }

    @SuppressWarnings("unchecked")
    private String extractVersion(String body) {
        try {
            Map<String, Object> root = json.readValue(body, Map.class);
            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null) return null;
            Map<String, Object> meta = (Map<String, Object>) data.get("metadata");
            if (meta == null) return null;
            Object v = meta.get("version");
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Instant extractCreated(String body) {
        try {
            Map<String, Object> root = json.readValue(body, Map.class);
            Map<String, Object> data = (Map<String, Object>) root.get("data");
            Map<String, Object> meta = data == null ? null : (Map<String, Object>) data.get("metadata");
            String ts = meta == null ? null : (String) meta.get("created_time");
            return ts == null ? null : Instant.parse(ts);
        } catch (Exception e) { return null; }
    }

    /* =================== SecretRef-based (do*) =================== */

    @Override protected SecretValue doGet(SecretRef ref, ReadOptions opts) {
        String url = dataUrl(ref.fqn());
        var request = req(URI.create(url)).GET().build();
        try {
            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 404) {
                if (opts.failIfMissing()) throw new RuntimeException("Vault not found: " + ref.fqn());
                return new SecretValue((char[]) null, "text/plain");
            }
            if (resp.statusCode() >= 300) throw new RuntimeException("Vault get failed " + resp.statusCode());
            String val = extractValue(resp.body());
            return new SecretValue(val == null ? null : val.toCharArray(), "text/plain");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override protected void doPut(SecretRef ref, SecretValue value, WriteOptions opts) {
        String url = dataUrl(ref.fqn());
        String payload;
        if (value.bytes().isPresent()) {
            String b64 = Base64.getEncoder().encodeToString(value.bytes().get());
            payload = "{\"data\":{\"value_b64\":\"" + b64 + "\"}}";
        } else {
            String v = new String(value.text().orElse("".toCharArray()));
            payload = "{\"data\":{\"value\":\"" + v.replace("\"","\\\"") + "\"}}";
        }
        var request = req(URI.create(url)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        try {
            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) throw new RuntimeException("Vault put failed " + resp.statusCode() + ": " + resp.body());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override protected void doDelete(SecretRef ref, boolean deleteVersions) {
        String url = (deleteVersions ? metaUrl(ref.fqn()) : dataUrl(ref.fqn()));
        var request = req(URI.create(url)).DELETE().build();
        try { http.send(request, HttpResponse.BodyHandlers.discarding()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override protected SecretMetadata doStat(SecretRef ref) {
        // Retorna metadata mínima; pode enriquecer consumindo /metadata/<path>
        return new SecretMetadata(ref, "Vault", null, List.of(), Map.of());
    }

    @Override protected void doEnableVersion(SecretRef ref, String versionId, boolean enabled) {
        // KV v2 não tem “enable”; há delete/undelete/destroy de versões (não implementado aqui).
    }

    @Override protected String doRotate(SecretRef ref, RotationPolicy policy) {
        // Exemplo simples: regrava o valor atual (ou um UUID) — substitua por gerador real
        putSecret(ref, new SecretValue(UUID.randomUUID().toString().toCharArray(), "text/plain"), WriteOptions.upsert());
        return "vault-" + System.currentTimeMillis();
    }

    /* =================== SecretId-based (tipada) =================== */

    @Override
    public Secret<String> getString(SecretId id, ReadOptions opts) {
        final String path = normPath(id.namespace(), id.name());
        return withSpan("getString", id, () -> {
            var request = req(URI.create(dataUrl(path))).GET().build();
            try {
                var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 404) {
                    if (opts.failIfMissing()) throw new RuntimeException("Vault not found: " + path);
                    return new Secret<>(id, SecretType.STRING, null, null, "current", null, null, Map.of(), null);
                }
                if (resp.statusCode() >= 300) throw new RuntimeException("Vault get failed " + resp.statusCode());
                String body = resp.body();
                String value = extractValue(body);
                String ver = extractVersion(body);
                Instant created = extractCreated(body);
                return new Secret<>(id, SecretType.STRING, value, ver, "current", created, null, Map.of(), ver);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Override
    public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions opts) {
        var s = getString(id, opts);
        if (s.value() == null) return new Secret<>(id, SecretType.JSON, Map.of(), s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        try {
            Map<String, Object> map = json.readValue(s.value(), new TypeReference<Map<String, Object>>() {});
            if (!opts.fieldsWhitelist().isEmpty()) map.keySet().removeIf(k -> !opts.fieldsWhitelist().contains(k));
            return new Secret<>(id, SecretType.JSON, map, s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        } catch (Exception e) { throw new RuntimeException("Invalid JSON for Vault secret: " + id.name(), e); }
    }

    @Override
    public Secret<byte[]> getBinary(SecretId id, ReadOptions opts) {
        final String path = normPath(id.namespace(), id.name());
        return withSpan("getBinary", id, () -> {
            var request = req(URI.create(dataUrl(path))).GET().build();
            try {
                var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 404) {
                    if (opts.failIfMissing()) throw new RuntimeException("Vault not found: " + path);
                    return new Secret<>(id, SecretType.BINARY, null, null, "current", null, null, Map.of(), null);
                }
                if (resp.statusCode() >= 300) throw new RuntimeException("Vault get failed " + resp.statusCode());
                String body = resp.body();
                byte[] value = extractBinary(body);
                String ver = extractVersion(body);
                Instant created = extractCreated(body);
                return new Secret<>(id, SecretType.BINARY, value, ver, "current", created, null, Map.of(), ver);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Override
    public Secret<String> put(PutSecretRequest req) {
        final String path = normPath(req.id().namespace(), req.id().name());
        return withSpan("put", req.id(), () -> {
            Map<String, Object> inner;
            if (req.type() == SecretType.BINARY && req.value() instanceof byte[] b) {
                inner = Map.of("value_b64", Base64.getEncoder().encodeToString(b));
            } else {
                inner = Map.of("value", String.valueOf(req.value()));
            }
            String payload;
            try { payload = json.writeValueAsString(Map.of("data", inner)); }
            catch (Exception e) { throw new RuntimeException(e); }

            var request = req(URI.create(dataUrl(path))).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            try {
                var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 300) throw new RuntimeException("Vault put failed " + resp.statusCode() + ": " + resp.body());
                String ver = "v" + System.currentTimeMillis(); // simples; pegue metadata real com um GET se quiser
                return new Secret<>(req.id(), req.type(),
                        req.type()==SecretType.BINARY ? "<binary>" : String.valueOf(req.value()),
                        ver, "current", Instant.now(), req.expiresAt(), req.tags(), ver);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Override
    public void delete(SecretId id, boolean deleteAllVersions) {
        final String path = normPath(id.namespace(), id.name());
        withSpan("delete", id, () -> {
            String url = deleteAllVersions ? metaUrl(path) : dataUrl(path);
            var request = req(URI.create(url)).DELETE().build();
            try {
                var resp = http.send(request, HttpResponse.BodyHandlers.discarding());
                if (resp.statusCode() >= 300 && resp.statusCode() != 404) {
                    throw new RuntimeException("Vault delete failed " + resp.statusCode());
                }
            } catch (Exception e) { throw new RuntimeException(e); }
            return null;
        });
    }

    @Override
    public List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor) {
        // Simplificado: retorna “versão atual” via GET; para histórico real, chamar /metadata/<path> e ler "versions"
        return List.of(getString(id, ReadOptions.defaults()));
    }

    @Override
    public void promoteStage(SecretId id, String fromStage, String toStage) {
        // KV v2 não tem stages; noop
    }

    @Override
    public Secret<String> rotate(SecretId id, RotationPolicy policy, java.util.function.Supplier<Object> generator) {
        Object newVal = generator.get();
        PutSecretRequest preq = PutSecretRequest.of(id)
                .type((newVal instanceof byte[]) ? SecretType.BINARY :
                        (newVal instanceof Map<?, ?>) ? SecretType.JSON : SecretType.STRING)
                .value(newVal)
                .build();
        return put(preq);
    }

    @Override
    public ListResult list(String namespace, String prefix, int pageSize, String cursor) {
        // GET /v1/<mount>/metadata/<prefix>?list=true
        String path = normPath(namespace, Optional.ofNullable(prefix).orElse(""));
        String url = metaUrl(path) + "?list=true";
        var request = req(URI.create(url)).GET().build();
        try {
            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 404) return new ListResult(List.of(), null, false);
            if (resp.statusCode() >= 300) throw new RuntimeException("Vault list failed " + resp.statusCode());

            // response: { "data": { "keys": ["a","b/"] } }
            Map<String, Object> root = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
            @SuppressWarnings("unchecked")
            Map<String,Object> data = (Map<String,Object>) root.get("data");
            @SuppressWarnings("unchecked")
            List<String> keys = data != null ? (List<String>) data.getOrDefault("keys", List.of()) : List.of();

            var items = new ArrayList<SecretId>();
            for (String k : keys) {
                // remove slash final "dir-like" e mapeia para SecretId(ns, name)
                String name = k.endsWith("/") ? k.substring(0, k.length()-1) : k;
                if (name.isBlank()) continue;
                items.add(SecretId.of(Optional.ofNullable(namespace).orElse("/"), name));
            }
            return new ListResult(items, null, false);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /* =================== Async tipada =================== */

    @Override public CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getString(id, opts));
    }
    @Override public CompletableFuture<Secret<Map<String, Object>>> getJsonAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getJson(id, opts));
    }
    @Override public CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getBinary(id, opts));
    }
}

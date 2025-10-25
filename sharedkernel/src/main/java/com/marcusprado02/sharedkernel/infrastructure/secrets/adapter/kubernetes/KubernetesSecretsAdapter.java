package com.marcusprado02.sharedkernel.infrastructure.secrets.adapter.kubernetes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.marcusprado02.sharedkernel.infrastructure.secrets.ListResult;
import com.marcusprado02.sharedkernel.infrastructure.secrets.ReadOptions;
import com.marcusprado02.sharedkernel.infrastructure.secrets.PutSecretRequest;
import com.marcusprado02.sharedkernel.infrastructure.secrets.RotationPolicy;
import com.marcusprado02.sharedkernel.infrastructure.secrets.Secret;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretId;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretMetadata;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretRef;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretType;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretValue;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretVersion;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretsAdapter;
import com.marcusprado02.sharedkernel.infrastructure.secrets.WriteOptions;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;

public class KubernetesSecretsAdapter implements SecretsAdapter {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final CoreV1Api api;
    private final String k8sNamespace;

    public KubernetesSecretsAdapter(CoreV1Api api, String namespace) {
        this.api = Objects.requireNonNull(api);
        this.k8sNamespace = Objects.requireNonNull(namespace);
    }

    /* ===================== Helpers ===================== */

    private static byte[] toBytes(Object value, SecretType type) {
        if (value == null) return new byte[0];
        if (type == SecretType.BINARY && value instanceof byte[] b) return b;
        if (value instanceof String s) return s.getBytes(StandardCharsets.UTF_8);
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String fromBytesToString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] extractBytes(Map<String, byte[]> byteMap, Map<String, ?> otherMap, String key) {
        if (byteMap != null && byteMap.containsKey(key)) return byteMap.get(key);
        if (otherMap != null && otherMap.containsKey(key)) {
            Object v = otherMap.get(key);
            if (v == null) return null;
            if (v instanceof ByteBuffer bb) {
                byte[] out = new byte[bb.remaining()];
                bb.slice().get(out);
                return out;
            }
            if (v instanceof String s) return s.getBytes(StandardCharsets.UTF_8);
            if (v instanceof byte[] b) return b;
        }
        return null;
    }

    private V1Secret getOrNull(String name) {
        try {
            return api.readNamespacedSecret(name, k8sNamespace, null);
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("Not Found") || msg.contains("404")) return null;
            throw new RuntimeException("K8s read secret failed: " + msg, e);
        }
    }

    private void createOrReplace(V1Secret secret) {
        try {
            V1Secret existing = getOrNull(secret.getMetadata().getName());
            if (existing == null) {
                api.createNamespacedSecret(k8sNamespace, secret, null, null, null, null);
            } else {
                api.replaceNamespacedSecret(secret.getMetadata().getName(), k8sNamespace, secret, null, null, null, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("K8s write secret failed: " + e.getMessage(), e);
        }
    }

    private void ensureKeyMap(V1Secret s) {
        if (s.getData() == null && s.getImmutable() == null) {
            s.setData(new java.util.HashMap<>());
        }
    }

    private static String keyInsideSecret(SecretId id) {
        // Convenção: usar namespace como "key" dentro do Secret
        return Optional.ofNullable(id.namespace()).orElse("value");
    }

    private static SecretId toId(SecretRef ref) {
        // environment -> namespace ; path -> name
        String ns = Optional.ofNullable(ref.environment()).orElse("/");
        return SecretId.of(ns, Objects.requireNonNull(ref.path(), "SecretRef.path must not be null"));
    }

    /* ===================== API tipada (SecretId) ===================== */

    @Override
    public Secret<String> getString(SecretId id, ReadOptions opts) {
        V1Secret sec = getOrNull(id.name());
        if (sec == null) {
            if (opts.failIfMissing()) throw new IllegalArgumentException("Secret not found: " + id.name());
            return new Secret<>(id, SecretType.STRING, null, null, null, null, null, Map.of(), null);
        }

        String key = keyInsideSecret(id);
        byte[] raw = extractBytes(sec.getData(), sec.getStringData(), key);
        if (raw == null) {
            String s = sec.getStringData() != null ? sec.getStringData().get(key) : null;
            if (s != null) raw = s.getBytes(StandardCharsets.UTF_8);
        }
        String val = raw == null ? null : fromBytesToString(raw);

        String version = sec.getMetadata() != null ? sec.getMetadata().getResourceVersion() : null;
        Instant created = sec.getMetadata() != null && sec.getMetadata().getCreationTimestamp() != null
                ? sec.getMetadata().getCreationTimestamp().toInstant() : null;

        return new Secret<>(id, SecretType.STRING, val, version, "current", created, null, Map.of(), version);
    }

    @Override
    public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions opts) {
        Secret<String> s = getString(id, opts);
        if (s.value() == null) {
            return new Secret<>(id, SecretType.JSON, Map.of(), s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        }
        try {
            Map<String, Object> map = JSON.readValue(s.value(), new TypeReference<Map<String, Object>>() {});
            if (!opts.fieldsWhitelist().isEmpty()) {
                map.keySet().removeIf(k -> !opts.fieldsWhitelist().contains(k));
            }
            return new Secret<>(id, SecretType.JSON, map, s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON in Kubernetes Secret: " + id.name(), e);
        }
    }

    @Override
    public Secret<byte[]> getBinary(SecretId id, ReadOptions opts) {
        V1Secret sec = getOrNull(id.name());
        if (sec == null) {
            if (opts.failIfMissing()) throw new IllegalArgumentException("Secret not found: " + id.name());
            return new Secret<>(id, SecretType.BINARY, null, null, null, null, null, Map.of(), null);
        }
        String key = keyInsideSecret(id);
        byte[] raw = extractBytes(sec.getData(), sec.getStringData(), key);
        String version = sec.getMetadata() != null ? sec.getMetadata().getResourceVersion() : null;
        Instant created = sec.getMetadata() != null && sec.getMetadata().getCreationTimestamp() != null
                ? sec.getMetadata().getCreationTimestamp().toInstant() : null;
        return new Secret<>(id, SecretType.BINARY, raw, version, "current", created, null, Map.of(), version);
    }

    @Override
    public Secret<String> put(PutSecretRequest req) {
        SecretId id = req.id();
        String name = id.name();
        String key = keyInsideSecret(id);

        V1Secret sec = Optional.ofNullable(getOrNull(name)).orElseGet(() -> {
            V1Secret s = new V1Secret();
            s.setMetadata(new V1ObjectMeta().name(name).namespace(k8sNamespace));
            s.setType("Opaque");
            s.setData(new java.util.HashMap<>());
            return s;
        });

        ensureKeyMap(sec);

        byte[] payload = toBytes(req.value(), req.type());
        if (sec.getData() == null) sec.setData(new java.util.HashMap<>());
        sec.getData().put(key, payload);

        createOrReplace(sec);

        String version = sec.getMetadata() != null ? sec.getMetadata().getResourceVersion() : null;
        return new Secret<>(id, req.type(),
                req.type() == SecretType.BINARY ? "<binary>" : new String(payload, StandardCharsets.UTF_8),
                version, "current", Instant.now(), req.expiresAt(), req.tags(), version);
    }

    @Override
    public void delete(SecretId id, boolean deleteAllVersions) {
        try {
            api.deleteNamespacedSecret(id.name(), k8sNamespace, null, null, null, null, null, null);
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (!(msg.contains("Not Found") || msg.contains("404"))) {
                throw new RuntimeException("K8s delete secret failed: " + msg, e);
            }
        }
    }

    @Override
    public List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor) {
        // Kubernetes não versiona Secrets; retornamos uma “versão” sintética (resourceVersion)
        Secret<String> s = getString(id, ReadOptions.defaults());
        return List.of(s);
    }

    @Override
    public void promoteStage(SecretId id, String fromStage, String toStage) {
        // Não aplicável em K8s; noop
    }

    @Override
    public Secret<String> rotate(SecretId id, RotationPolicy policy, java.util.function.Supplier<Object> generator) {
        Object newVal = generator.get();
        PutSecretRequest req = PutSecretRequest.of(id)
                .type((newVal instanceof byte[]) ? SecretType.BINARY :
                        (newVal instanceof Map<?, ?>) ? SecretType.JSON : SecretType.STRING)
                .value(newVal)
                .build();
        return put(req);
    }

    /* ===================== Listagem (ajuste assinatura v19+) ===================== */

    @Override
    public ListResult list(String namespace, String prefix, int pageSize, String cursor) {
        try {
            // Adapted to client signature that includes extra boolean/timeout parameters (varies by client version)
            V1SecretList list = api.listNamespacedSecret(
                    k8sNamespace,
                    null,                              // pretty
                    null,                 // allowWatchBookmarks / boolean placeholder
                    cursor,                                   // _continue
                    null,                       // fieldSelector
                    null,                       // labelSelector
                    (pageSize <= 0 ? null : pageSize),        // limit
                    null,                     // resourceVersion
                    null,                // resourceVersionMatch
                    false,                  // watch
                    null,                      // timeoutSeconds
                    false                               // boolean placeholder (client-specific)
            );

            var items = list.getItems().stream()
                    .map(s -> {
                        String name = s.getMetadata().getName();
                        String nsKey = Optional.ofNullable(namespace).orElse("/");
                        return SecretId.of(nsKey, name);
                    }).collect(Collectors.toList());

            String cont = (list.getMetadata() != null) ? list.getMetadata().getContinue() : null;
            boolean trunc = cont != null && !cont.isBlank();
            return new ListResult(items, cont, trunc);
        } catch (Exception e) {
            throw new RuntimeException("K8s list secrets failed: " + e.getMessage(), e);
        }
    }

    /* ===================== Async tipada (já existia) ===================== */

    @Override public CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions o) {
        return CompletableFuture.supplyAsync(() -> getString(id, o));
    }
    @Override public CompletableFuture<Secret<Map<String, Object>>> getJsonAsync(SecretId id, ReadOptions o) {
        return CompletableFuture.supplyAsync(() -> getJson(id, o));
    }
    @Override public CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions o) {
        return CompletableFuture.supplyAsync(() -> getBinary(id, o));
    }

    /* ===================== API por SecretRef (exigida por SecretsAdapter) ===================== */

    @Override
    public SecretValue getSecret(SecretRef ref, ReadOptions opts) {
        // Convenção: ler como texto (se precisar diferenciar binário, use contentType no SecretRef/opts)
        Secret<String> s = getString(toId(ref), opts);
        return new SecretValue(s.value() == null ? null : s.value().toCharArray(), "text/plain");
    }

    @Override
    public SecretMetadata stat(SecretRef ref) {
        var id = toId(ref);
        var s = getString(id, ReadOptions.defaults());
        var ver = Optional.ofNullable(s.versionId()).orElse("current");
        var created = s.createdAt() != null ? s.createdAt() : Instant.EPOCH;
        var versions = List.of(new SecretVersion(ver, created, s.value() != null, null));
        return new SecretMetadata(ref, backendName(), s.etag(), versions, Map.of());
    }

    @Override
    public void putSecret(SecretRef ref, SecretValue value, WriteOptions opts) {
        var id = toId(ref);
        Object payload;
        SecretType type;
        if (value.bytes().isPresent() || (value.contentType() != null && value.contentType().startsWith("application/octet-stream"))) {
            type = SecretType.BINARY;
            payload = value.bytes().orElseGet(() -> new String(value.text().orElse("".toCharArray())).getBytes(StandardCharsets.UTF_8));
        } else {
            type = SecretType.STRING;
            payload = new String(value.text().orElse("".toCharArray()));
        }
        put(PutSecretRequest.of(id).type(type).value(payload).build());
    }

    @Override
    public void deleteSecret(SecretRef ref, boolean deleteVersions) {
        delete(toId(ref), deleteVersions);
    }

    @Override
    public List<String> list(SecretRef prefix) {
        var lr = list(prefix.environment(), prefix.path(), 100, null);
        return lr.items().stream().map(si -> si.namespace() + "/" + si.name()).toList();
    }

    @Override
    public void enableVersion(SecretRef ref, String versionId, boolean enabled) {
        // K8s não suporta enable/disable per-version; noop
    }

    @Override
    public String rotate(SecretRef ref, RotationPolicy policy) {
        // Simples: reescreve o mesmo valor (ou gere novo externamente)
        var s = getSecret(ref, ReadOptions.defaults());
        putSecret(ref, s, WriteOptions.upsert());
        return "k8s-" + System.currentTimeMillis();
    }

    @Override
    public CompletableFuture<SecretValue> getSecretAsync(SecretRef ref, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getSecret(ref, opts));
    }

    @Override
    public CompletableFuture<Void> putSecretAsync(SecretRef ref, SecretValue value, WriteOptions opts) {
        return CompletableFuture.runAsync(() -> putSecret(ref, value, opts));
    }

    @Override public String backendName() { return "KubernetesSecretsAdapter"; }
}

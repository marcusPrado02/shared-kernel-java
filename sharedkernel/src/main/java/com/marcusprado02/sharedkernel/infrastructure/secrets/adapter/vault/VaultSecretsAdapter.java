package com.marcusprado02.sharedkernel.infrastructure.secrets.adapter.vault;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.infrastructure.secrets.*;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;

/**
 * VaultSecretsAdapter – KV v2 (+ Transit opcional).
 * Convenções:
 * - Mount do KV v2 padrão: "secret". Paths:
 *   - read:   /v1/{mount}/data/{path}
 *   - write:  /v1/{mount}/data/{path}  (body: {"data":{...}})
 *   - delete: /v1/{mount}/data/{path}
 *   - delete metadata (todas versões): /v1/{mount}/metadata/{path}
 * - Guardamos valores sob a chave "value" (STRING/JSON string). Para BINARY, usamos base64 em "value_b64".
 */
public class VaultSecretsAdapter implements SecretsAdapter {

    private final Vault vault;
    private final String kvMount; // ex.: "secret"
    private final ObjectMapper json = new ObjectMapper();

    public VaultSecretsAdapter(Vault vaultClient) {
        this(vaultClient, "secret");
    }

    public VaultSecretsAdapter(Vault vaultClient, String kvMount) {
        this.vault = Objects.requireNonNull(vaultClient, "vault");
        this.kvMount = (kvMount == null || kvMount.isBlank()) ? "secret" : kvMount.replaceAll("^/|/$", "");
    }

    /* ======================= Helpers ======================= */

    private String dataPath(String path)     { return kvMount + "/data/" + normalize(path); }
    private String metaPath(String path)     { return kvMount + "/metadata/" + normalize(path); }
    private String listPath(String prefix)   { return kvMount + "/metadata/" + normalize(prefix); }

    private static String normalize(String p) {
        String s = Objects.requireNonNull(p, "path").replaceAll("^/+", "");
        return s;
    }

    private static SecretId toId(SecretRef ref) {
        // Convenção: environment -> namespace ; path -> name
        String ns = Optional.ofNullable(ref.environment()).orElse("/");
        return SecretId.of(ns, Objects.requireNonNull(ref.path(), "SecretRef.path must not be null"));
    }

    private static SecretValue svFromString(String s) {
        return new SecretValue(s == null ? null : s.toCharArray(), "text/plain");
    }
    private static SecretValue svFromBytes(byte[] b) {
        return new SecretValue(b == null ? new byte[0] : Arrays.copyOf(b, b.length), "application/octet-stream");
    }
    private static String asString(SecretValue v) {
        if (v == null) return null;
        return v.text().map(String::new)
                .orElseGet(() -> v.bytes().map(b -> new String(b, java.nio.charset.StandardCharsets.UTF_8)).orElse(null));
    }
    private static byte[] asBytes(SecretValue v) {
        if (v == null) return null;
        return v.bytes().orElseGet(() -> v.text().map(t -> new String(t).getBytes(java.nio.charset.StandardCharsets.UTF_8)).orElse(null));
    }

    /* ======================= SecretId API (tipada) ======================= */

    @Override
    public Secret<String> getString(SecretId id, ReadOptions opts) {
        var path = id.namespace() + "/" + id.name();
        try {
            var resp = vault.logical().read(dataPath(path));
            if (resp == null || resp.getData() == null) {
                if (opts.failIfMissing()) {
                    throw new RuntimeException("Vault secret not found: " + path);
                } else {
                    return new Secret<>(id, SecretType.STRING, null, null, "current", null, null, Map.of(), null);
                }
            }

            Map<String, String> flat = resp.getData(); // driver devolve strings
            // "data" e "metadata" vêm como JSON strings; parseie-as
            Map<String, Object> data = null, meta = null;
            try {
                String dataJson = flat.get("data");
                if (dataJson != null) data = json.readValue(dataJson, Map.class);
                String metaJson = flat.get("metadata");
                if (metaJson != null) meta = json.readValue(metaJson, Map.class);
            } catch (Exception ex) {
                throw new RuntimeException("Vault KVv2 parse failed: " + path, ex);
            }

            String raw = (data == null) ? null : (String) data.get("value");
            String ver = (meta == null) ? null : String.valueOf(meta.get("version"));
            Instant created = null;
            if (meta != null && meta.get("created_time") instanceof String ts) {
                created = Instant.parse(ts);
            }

            return new Secret<>(id, SecretType.STRING, raw, ver, "current", created, null, Map.of(), ver);
        } catch (VaultException e) {
            if (e.getHttpStatusCode() == 404 && !opts.failIfMissing()) {
                return new Secret<>(id, SecretType.STRING, null, null, "current", null, null, Map.of(), null);
            }
            throw new RuntimeException("Vault getString failed: " + path, e);
        }
    }

    @Override
    public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions opts) {
        Secret<String> s = getString(id, opts);
        if (s.value() == null) return new Secret<>(id, SecretType.JSON, Map.of(), s.versionId(), s.stage(), s.createdAt(), null, Map.of(), s.etag());
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> map = json.readValue(s.value(), Map.class);
            if (!opts.fieldsWhitelist().isEmpty()) {
                map.keySet().removeIf(k -> !opts.fieldsWhitelist().contains(k));
            }
            return new Secret<>(id, SecretType.JSON, map, s.versionId(), s.stage(), s.createdAt(), null, Map.of(), s.etag());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid JSON in Vault secret: " + id.name(), ex);
        }
    }

    @Override
    public Secret<byte[]> getBinary(SecretId id, ReadOptions opts) {
        var path = id.namespace() + "/" + id.name();
        try {
            var resp = vault.logical().read(dataPath(path));
            if (resp == null || resp.getData() == null) { 
                if (opts.failIfMissing()) {
                    throw new RuntimeException("Vault secret not found: " + path);
                } else {
                    return new Secret<>(id, SecretType.BINARY, null, null, "current", null, null, Map.of(), null);
                }
             }

            Map<String,String> flat = resp.getData();
            Map<String,Object> data = null, meta = null;
            try {
                String dataJson = flat.get("data");
                if (dataJson != null) data = json.readValue(dataJson, Map.class);
                String metaJson = flat.get("metadata");
                if (metaJson != null) meta = json.readValue(metaJson, Map.class);
            } catch (Exception ex) {
                throw new RuntimeException("Vault KVv2 parse failed: " + path, ex);
            }

            byte[] raw = null;
            if (data != null) {
                Object b64 = data.get("value_b64");
                if (b64 instanceof String s) {
                    raw = Base64.getDecoder().decode(s);
                } else {
                    Object s = data.get("value");
                    if (s instanceof String str) raw = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            String ver = (meta == null) ? null : String.valueOf(meta.get("version"));
            Instant created = (meta != null && meta.get("created_time") instanceof String ts) ? Instant.parse(ts) : null;

            return new Secret<>(id, SecretType.BINARY, raw, ver, "current", created, null, Map.of(), ver);
        } catch (VaultException e) {
            if (e.getHttpStatusCode() == 404 && !opts.failIfMissing()) {
                return new Secret<>(id, SecretType.BINARY, null, null, "current", null, null, Map.of(), null);
            }
            throw new RuntimeException("Vault getBinary failed: " + path, e);
        }
    }

    @Override
    public Secret<String> put(PutSecretRequest req) {
        var path = req.id().namespace() + "/" + req.id().name();
        Map<String,Object> stored;
        if (req.type() == SecretType.BINARY && req.value() instanceof byte[] b) {
            stored = Map.of("data", Map.of("value_b64", Base64.getEncoder().encodeToString(b)));
        } else {
            stored = Map.of("data", Map.of("value", String.valueOf(req.value())));
        }
        try {
            vault.logical().write(dataPath(path), stored);
            String ver = "v" + System.currentTimeMillis(); // sem roundtrip; opcionalmente, faça um read pós-put para pegar metadata real
            return new Secret<>(req.id(), req.type(),
                    req.type()==SecretType.BINARY ? "<binary>" : String.valueOf(req.value()),
                    ver, "current", Instant.now(), req.expiresAt(), req.tags(), ver);
        } catch (VaultException e) {
            throw new RuntimeException("Vault put failed: " + path, e);
        }
    }

    @Override
    public void delete(SecretId id, boolean deleteAllVersions) {
        var path = id.namespace() + "/" + id.name();
        try {
            if (deleteAllVersions) {
                vault.logical().delete(metaPath(path)); // delete metadata = remove todas as versões
            } else {
                vault.logical().delete(dataPath(path)); // marca versão deletada (soft) no KV v2
            }
        } catch (VaultException e) {
            if (e.getHttpStatusCode() != 404) {
                throw new RuntimeException("Vault delete failed: " + path, e);
            }
        }
    }

    @Override
    public List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor) {
        // KV v2 tem metadata com versions; simplificando: devolvemos a versão atual via getString
        return List.of(getString(id, ReadOptions.defaults()));
    }

    @Override
    public void promoteStage(SecretId id, String fromStage, String toStage) {
        // Não há "stages" nativos em KV v2; noop
    }

    @Override
    public Secret<String> rotate(SecretId id, RotationPolicy policy, java.util.function.Supplier<Object> generator) {
        Object newVal = generator.get();
        PutSecretRequest req = PutSecretRequest.of(id)
                .type((newVal instanceof byte[]) ? SecretType.BINARY :
                      (newVal instanceof Map<?,?>) ? SecretType.JSON : SecretType.STRING)
                .value(newVal)
                .build();
        return put(req);
    }

    @Override
    public ListResult list(String namespace, String prefix, int pageSize, String cursor) {
        // Lista chaves sob um prefixo (KV v2 requer /metadata/)
        try {
            var resp = vault.logical().list(listPath(namespace == null ? prefix : namespace + "/" + prefix));
            var keys = Optional.ofNullable(resp).map(r -> r.getListData()).orElse(List.of());
            var items = keys.stream().map(k -> SecretId.of(Optional.ofNullable(namespace).orElse("/"), k)).toList();
            return new ListResult(items, null, false);
        } catch (VaultException e) {
            if (e.getHttpStatusCode() == 404) return new ListResult(List.of(), null, false);
            throw new RuntimeException("Vault list failed", e);
        }
    }

    @Override
    public CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getString(id, opts));
    }

    @Override
    public CompletableFuture<Secret<Map<String, Object>>> getJsonAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getJson(id, opts));
    }

    @Override
    public CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getBinary(id, opts));
    }

    /* ======================= API SecretRef/SecretValue ======================= */

    @Override
    public SecretValue getSecret(SecretRef ref, ReadOptions opts) {
        // Padrão: ler como STRING
        var s = getString(toId(ref), opts);
        return svFromString(s.value());
    }

    @Override
    public SecretMetadata stat(SecretRef ref) {
        // Monta metadado simples a partir do read (KV v2 metadata exigiria chamada dedicada se quiser mais riqueza)
        var s = getString(toId(ref), ReadOptions.defaults());
        return new SecretMetadata(
                ref,
                backendName(),
                s.etag(),
                List.of(new SecretVersion(s.versionId(), s.createdAt(), true, null)),
                Map.of()
        );
    }

    @Override
    public void putSecret(SecretRef ref, SecretValue value, WriteOptions opts) {
        var id = toId(ref);
        String ct = value.contentType();
        Object payload;
        SecretType type;
        if (ct != null && ct.startsWith("application/octet-stream")) {
            type = SecretType.BINARY; payload = asBytes(value);
        } else {
            type = SecretType.STRING; payload = asString(value); // JSON string é aceito aqui
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
        // Não suportado no KV v2; noop
    }

    @Override
    public String rotate(SecretRef ref, RotationPolicy policy) {
        var current = getSecret(ref, ReadOptions.defaults());
        putSecret(ref, current, WriteOptions.upsert());
        return "vault-" + System.currentTimeMillis();
    }

    @Override
    public CompletableFuture<SecretValue> getSecretAsync(SecretRef ref, ReadOptions opts) {
        return CompletableFuture.supplyAsync(() -> getSecret(ref, opts));
    }

    @Override
    public CompletableFuture<Void> putSecretAsync(SecretRef ref, SecretValue value, WriteOptions opts) {
        return CompletableFuture.runAsync(() -> putSecret(ref, value, opts));
    }

    @Override
    public Optional<CryptoCapabilities> crypto() {
        return Optional.of(new CryptoCapabilities() {
            @Override public byte[] encrypt(String key, byte[] plaintext, Map<String, String> aad) {
                try {
                    var r = vault.logical().write("transit/encrypt/" + key,
                            Map.of("plaintext", Base64.getEncoder().encodeToString(plaintext)));
                    String ct = (String) r.getData().get("ciphertext");
                    return ct.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                } catch (VaultException e) { throw new RuntimeException(e); }
            }
            @Override public byte[] decrypt(String key, byte[] ciphertext, Map<String, String> aad) {
                try {
                    var r = vault.logical().write("transit/decrypt/" + key,
                            Map.of("ciphertext", new String(ciphertext, java.nio.charset.StandardCharsets.UTF_8)));
                    String ptB64 = (String) r.getData().get("plaintext");
                    return Base64.getDecoder().decode(ptB64);
                } catch (VaultException e) { throw new RuntimeException(e); }
            }
            @Override public byte[] sign(String key, byte[] message) { throw new UnsupportedOperationException("transit/sign not implemented"); }
            @Override public boolean verify(String key, byte[] message, byte[] signature) { return false; }
        });
    }

    @Override public String backendName() { return "VaultSecretsAdapter"; }
}
